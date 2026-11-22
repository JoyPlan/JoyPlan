package com.joyplan.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AiPlanService {

    private static final Logger log = LoggerFactory.getLogger(AiPlanService.class);

    private final ObjectMapper mapper;
    private final String apiKey;
    private final String model;
    private final RestClient restClient;

    public AiPlanService(ObjectMapper mapper,
                         @Value("${joyplan.ai.api-key:}") String apiKey,
                         @Value("${joyplan.ai.model:gemini-2.5-flash}") String model,
                         @Value("${joyplan.ai.timeout-seconds:20}") int timeoutSeconds) {
        this.mapper = mapper;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutSeconds * 1000);
        factory.setReadTimeout(timeoutSeconds * 1000);
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .requestFactory(factory)
                .build();
    }

    public boolean isEnabled() {
        return !apiKey.isEmpty();
    }

    public Optional<AiDraft> compose(PlanningContext ctx, List<ScoredVenue> candidates) {
        if (!isEnabled() || candidates.isEmpty()) {
            return Optional.empty();
        }
        try {
            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", buildPrompt(ctx, candidates))))),
                    "generationConfig", Map.of("responseMimeType", "application/json", "temperature", 0.4));
            String response = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);
            return parse(response);
        } catch (Exception e) {
            log.warn("AI plan generation failed, using the rule engine instead: {}", e.getMessage());
            return Optional.empty();
        }
    }

    Optional<AiDraft> parse(String response) throws Exception {
        if (response == null || response.isBlank()) {
            return Optional.empty();
        }
        JsonNode root = mapper.readTree(response);
        String text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText("");
        text = text.replace("```json", "").replace("```", "").trim();
        if (text.isEmpty()) {
            return Optional.empty();
        }
        JsonNode plan = mapper.readTree(text);
        List<AiDraft.Stop> stops = new ArrayList<>();
        for (JsonNode stop : plan.path("stops")) {
            if (stop.hasNonNull("venueId")) {
                stops.add(new AiDraft.Stop(stop.get("venueId").asLong(), stop.path("reason").asText("")));
            }
        }
        if (stops.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new AiDraft(plan.path("summary").asText(""), stops));
    }

    String buildPrompt(PlanningContext ctx, List<ScoredVenue> candidates) {
        String ages = ctx.childAges().stream().map(String::valueOf).collect(Collectors.joining(", "));
        String interests = ctx.interests().isEmpty() ? "none given"
                : ctx.interests().stream().map(i -> i.getLabel()).collect(Collectors.joining(", "));
        String food = ctx.foodTags().isEmpty() ? "no preference"
                : ctx.foodTags().stream().map(f -> f.getLabel()).collect(Collectors.joining(", "));
        StringBuilder sb = new StringBuilder();
        sb.append("You are JoyPlan, a family day planner in Abu Dhabi. Build ONE ordered day plan for a family.\n");
        sb.append("Children's ages: ").append(ages).append(". Adults: ").append(ctx.adults()).append(".\n");
        sb.append("Mood: ").append(ctx.request().getMood().getLabel())
                .append(". Energy: ").append(ctx.energy().getLabel()).append(".\n");
        sb.append("Interests: ").append(interests).append(". Food preferences: ").append(food).append(".\n");
        sb.append("Time window: ").append(PlanMath.toTime(ctx.startMinute())).append(" to ")
                .append(PlanMath.toTime(ctx.endMinute())).append(" (").append(ctx.totalMinutes()).append(" minutes).\n");
        sb.append("Total budget for the whole family: AED ").append(ctx.budget()).append(".\n");
        sb.append("Include a meal: ").append(ctx.request().isIncludeMeal() ? "yes" : "no").append(".\n");
        sb.append("Rules: use ONLY venueIds from the list below; total cost of chosen stops must not exceed the budget; ");
        sb.append("sum of durations plus about 15 minutes travel between different areas must fit the time window; ");
        sb.append("add a break after high-energy activities; choose 2 to 6 stops; ignore whether a venue is sponsored.\n");
        sb.append("Candidates (venueId | category | name | minutes | family cost AED | distance | opening | energy | ages):\n");
        for (ScoredVenue c : candidates) {
            sb.append(c.venue().getId()).append(" | ")
                    .append(c.venue().getCategory()).append(" | ")
                    .append(c.venue().getName()).append(" | ")
                    .append(c.venue().getDurationMinutes()).append(" | ")
                    .append(c.cost()).append(" | ")
                    .append(PlanMath.formatKm(c.distanceKm())).append(" | ")
                    .append(c.venue().getOpenTime()).append("-").append(c.venue().getCloseTime()).append(" | ")
                    .append(c.venue().getEnergyLevel()).append(" | ")
                    .append(c.venue().getAgeRange()).append("\n");
        }
        sb.append("Reply with JSON only: {\"summary\": \"one friendly sentence for the parents\", ");
        sb.append("\"stops\": [{\"venueId\": 1, \"reason\": \"one short sentence explaining why this suits the children\"}]}");
        return sb.toString();
    }
}
