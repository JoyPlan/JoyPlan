package com.joyplan.service;

import com.joyplan.model.Child;
import com.joyplan.model.EnergyLevel;
import com.joyplan.model.FoodTag;
import com.joyplan.model.Interest;
import com.joyplan.model.Mood;
import com.joyplan.model.OutingRequest;
import com.joyplan.model.Plan;
import com.joyplan.model.PlanItem;
import com.joyplan.model.PlanStatus;
import com.joyplan.model.User;
import com.joyplan.model.Venue;
import com.joyplan.model.VenueCategory;
import com.joyplan.repository.ChildRepository;
import com.joyplan.repository.PlanRepository;
import com.joyplan.repository.UserRepository;
import com.joyplan.repository.VenueRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class PlanService {

    private static final int AI_CANDIDATES_PER_CATEGORY = 6;
    private static final int TIRED_MAX_MINUTES = 90;
    private static final int TIRED_MAX_DISTANCE_KM = 8;

    private final PlanRepository planRepository;
    private final ChildRepository childRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final RecommendationService recommendations;
    private final RulePlanner planner;
    private final PlanValidator validator;
    private final AiPlanService aiPlanService;

    public PlanService(PlanRepository planRepository, ChildRepository childRepository, UserRepository userRepository,
                       VenueRepository venueRepository, RecommendationService recommendations, RulePlanner planner,
                       PlanValidator validator, AiPlanService aiPlanService) {
        this.planRepository = planRepository;
        this.childRepository = childRepository;
        this.userRepository = userRepository;
        this.venueRepository = venueRepository;
        this.recommendations = recommendations;
        this.planner = planner;
        this.validator = validator;
        this.aiPlanService = aiPlanService;
    }

    public Plan generate(User user, OutingRequest request) {
        List<Child> kids = childrenOf(user);
        Plan plan = new Plan();
        plan.setOwner(user);
        plan.setRequest(request);
        plan.setTitle(defaultTitle(request));
        fill(plan, kids, Set.of());
        return planRepository.saveAndFlush(plan);
    }

    public Plan regenerate(Long planId, User user) {
        Plan plan = owned(planId, user);
        Set<Long> avoid = plan.getItems().stream().map(i -> i.getVenue().getId()).collect(Collectors.toSet());
        plan.setTiredAdjusted(false);
        fill(plan, childrenOf(user), avoid);
        return planRepository.saveAndFlush(plan);
    }

    @Transactional(readOnly = true)
    public Alternatives alternatives(Long planId, Long itemId, User user) {
        Plan plan = owned(planId, user);
        PlanItem target = item(plan, itemId);
        PlanningContext ctx = baseContext(plan.getRequest(), childrenOf(user), Set.of());
        VenueCategory category = target.getVenue().getCategory();
        List<ScoredVenue> options = validReplacements(plan, target, recommendations.rank(ctx, category), ctx);
        boolean extended = false;
        if (options.size() < 2) {
            double wider = plan.getRequest().getMaxDistanceKm() * 1.5;
            options = validReplacements(plan, target, recommendations.rank(ctx, category, wider), ctx);
            extended = true;
        }
        return new Alternatives(target, options.stream().limit(3).toList(), extended);
    }

    public Plan replace(Long planId, Long itemId, Long venueId, User user) {
        Plan plan = owned(planId, user);
        PlanItem target = item(plan, itemId);
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new NotFoundException("Venue not found."));
        if (!venue.isActive() || venue.getCategory() != target.getVenue().getCategory()) {
            throw new IllegalArgumentException("That option cannot replace this stop.");
        }
        PlanningContext ctx = baseContext(plan.getRequest(), childrenOf(user), Set.of());
        ScoredVenue scored = recommendations.score(ctx, venue, plan.getRequest().getMaxDistanceKm() * 1.5);
        List<PlanItem> items = trial(plan, target, venue, scored == null ? "Chosen by you." : scored.reason(), ctx);
        if (items == null) {
            throw new IllegalArgumentException("That option no longer fits your budget or time window.");
        }
        plan.replaceItems(items);
        return planRepository.saveAndFlush(plan);
    }

    public PlanItem toggleVisited(Long planId, Long itemId, User user) {
        Plan plan = owned(planId, user);
        PlanItem item = item(plan, itemId);
        item.setVisited(!item.isVisited());
        return item;
    }

    public String kidsAreTired(Long planId, User user) {
        Plan plan = owned(planId, user);
        List<PlanItem> visited = plan.getItems().stream().filter(PlanItem::isVisited).toList();
        List<PlanItem> remaining = plan.getItems().stream().filter(i -> !i.isVisited()).toList();
        if (remaining.isEmpty()) {
            return "All stops are already done, so the day is over. Time to head home and rest!";
        }
        OutingRequest original = plan.getRequest();
        int startMinute = visited.isEmpty() ? PlanMath.toMinutes(original.getStartTime())
                : PlanMath.toMinutes(visited.get(visited.size() - 1).getEndTime());
        int endMinute = Math.min(PlanMath.toMinutes(original.getEndTime()), startMinute + TIRED_MAX_MINUTES);
        int spent = visited.stream().mapToInt(PlanItem::getCost).sum();
        double lat = visited.isEmpty() ? original.getArea().getLatitude()
                : visited.get(visited.size() - 1).getVenue().getLatitude();
        double lng = visited.isEmpty() ? original.getArea().getLongitude()
                : visited.get(visited.size() - 1).getVenue().getLongitude();

        OutingRequest tired = original.copy();
        tired.setMood(Mood.TIRED);
        tired.setEnergy(EnergyLevel.LOW);
        tired.setMaxDistanceKm(Math.min(original.getMaxDistanceKm(), TIRED_MAX_DISTANCE_KM));
        tired.setIncludeMeal(false);

        PlanningContext ctx = context(tired, childrenOf(user), Set.of(), true, lat, lng,
                startMinute, endMinute, Math.max(0, original.getBudget() - spent));
        List<PlanItem> calm = planner.build(ctx);

        List<PlanItem> all = new ArrayList<>();
        visited.forEach(v -> all.add(v.copy()));
        all.addAll(calm);
        plan.replaceItems(all);
        plan.setTiredAdjusted(true);
        plan.setSummary("Adjusted for tired kids: the rest of the outing is now shorter, calmer and closer, ending by "
                + (all.isEmpty() ? PlanMath.toTime(startMinute) : all.get(all.size() - 1).getEndTime()) + ".");
        if (calm.isEmpty()) {
            return "No calm stop fits nearby, so the remaining stops were removed. Head home for a rest.";
        }
        return "Plan adjusted: " + calm.size() + " calm stop(s) nearby, finishing within " + TIRED_MAX_MINUTES + " minutes.";
    }

    public Plan save(Long planId, User user) {
        Plan plan = owned(planId, user);
        if (plan.getStatus() == PlanStatus.DRAFT) {
            plan.setStatus(PlanStatus.SAVED);
        }
        return planRepository.saveAndFlush(plan);
    }

    public Plan rate(Long planId, int rating, String comment, User user) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        Plan plan = owned(planId, user);
        plan.setRating(rating);
        plan.setRatingComment(comment == null ? null : comment.trim());
        plan.setStatus(PlanStatus.COMPLETED);
        return planRepository.saveAndFlush(plan);
    }

    public Plan reuse(Long planId, User user) {
        Plan source = accessible(planId, user);
        OutingRequest request = source.getRequest().copy();
        request.setOutingDate(LocalDate.now());
        return generate(user, request);
    }

    public void share(Long planId, String email, User user) {
        Plan plan = owned(planId, user);
        User target = userRepository.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .orElseThrow(() -> new IllegalArgumentException("No JoyPlan account uses that email."));
        if (target.getId().equals(user.getId())) {
            throw new IllegalArgumentException("You already own this plan.");
        }
        plan.getSharedWith().add(target);
    }

    public void delete(Long planId, User user) {
        planRepository.delete(owned(planId, user));
    }

    @Transactional(readOnly = true)
    public Plan accessible(Long planId, User user) {
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new NotFoundException("Plan not found."));
        if (!plan.isAccessibleBy(user)) {
            throw new AccessDeniedException("You cannot view this plan.");
        }
        return planRepository.saveAndFlush(plan);
    }

    @Transactional(readOnly = true)
    public List<Plan> plansOf(User user) {
        return planRepository.findByOwnerOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Plan> sharedWith(User user) {
        return planRepository.findBySharedWithContainingOrderByCreatedAtDesc(user);
    }

    private Plan owned(Long planId, User user) {
        Plan plan = planRepository.findById(planId).orElseThrow(() -> new NotFoundException("Plan not found."));
        if (!plan.isOwnedBy(user)) {
            throw new AccessDeniedException("Only the plan owner can change this plan.");
        }
        return planRepository.saveAndFlush(plan);
    }

    private PlanItem item(Plan plan, Long itemId) {
        return plan.getItems().stream().filter(i -> itemId != null && itemId.equals(i.getId())).findFirst()
                .orElseThrow(() -> new NotFoundException("Plan item not found."));
    }

    private List<Child> childrenOf(User user) {
        List<Child> kids = childRepository.findByParentOrderByAgeAsc(user);
        if (kids.isEmpty()) {
            throw new IllegalStateException("Please add at least one child to your family profile first.");
        }
        return kids;
    }

    private void fill(Plan plan, List<Child> kids, Set<Long> avoid) {
        OutingRequest request = plan.getRequest();
        PlanningContext ctx = baseContext(request, kids, avoid);
        List<PlanItem> items = null;
        String generatedBy = "JoyPlan recommendation engine";
        String summary = null;

        if (aiPlanService.isEnabled()) {
            Map<Long, ScoredVenue> candidates = new LinkedHashMap<>();
            for (VenueCategory category : VenueCategory.values()) {
                recommendations.rank(ctx, category).stream().limit(AI_CANDIDATES_PER_CATEGORY)
                        .forEach(c -> candidates.put(c.venue().getId(), c));
            }
            Optional<AiDraft> draft = aiPlanService.compose(ctx, new ArrayList<>(candidates.values()));
            if (draft.isPresent()) {
                List<Venue> ordered = new ArrayList<>();
                Map<Long, String> reasons = new HashMap<>();
                for (AiDraft.Stop stop : draft.get().stops()) {
                    ScoredVenue scored = candidates.get(stop.venueId());
                    if (scored != null && !ordered.contains(scored.venue())) {
                        ordered.add(scored.venue());
                        String reason = stop.reason() == null || stop.reason().isBlank() ? scored.reason() : stop.reason().trim();
                        if (scored.venue().isSponsored()) {
                            reason += " Sponsored listing: ranked by suitability only.";
                        }
                        reasons.put(scored.venue().getId(), reason);
                    }
                }
                List<PlanItem> aiItems = planner.sequence(ctx, ordered, reasons, false);
                if (aiItems.size() >= 2 && validator.isValid(aiItems, request)) {
                    items = aiItems;
                    generatedBy = "Gemini AI + JoyPlan checks";
                    summary = draft.get().summary();
                }
            }
        }
        if (items == null) {
            items = planner.build(ctx);
        }
        while (!items.isEmpty() && !validator.isValid(items, request)) {
            items.remove(items.size() - 1);
        }
        plan.replaceItems(items);
        plan.setGeneratedBy(generatedBy);
        plan.setSummary(summary == null || summary.isBlank() ? describe(plan, kids) : summary);
    }

    private List<ScoredVenue> validReplacements(Plan plan, PlanItem target, List<ScoredVenue> ranked, PlanningContext ctx) {
        Set<Long> inPlan = plan.getItems().stream().map(i -> i.getVenue().getId()).collect(Collectors.toSet());
        List<ScoredVenue> valid = new ArrayList<>();
        for (ScoredVenue candidate : ranked) {
            if (inPlan.contains(candidate.venue().getId())) {
                continue;
            }
            List<PlanItem> t = trial(plan, target, candidate.venue(), candidate.reason(), ctx);
            if (t != null) {
                                valid.add(candidate);
            } else {
                            }
        }
        return valid;
    }

    private List<PlanItem> trial(Plan plan, PlanItem target, Venue replacement, String reason, PlanningContext ctx) {
        List<Venue> ordered = new ArrayList<>();
        Map<Long, String> reasons = new HashMap<>();
        Set<Long> visitedVenues = new HashSet<>();
        for (PlanItem item : plan.getItems()) {
            Venue venue = item == target ? replacement : item.getVenue();
            ordered.add(venue);
            reasons.put(venue.getId(), item == target ? reason : item.getReason());
            if (item.isVisited() && item != target) {
                visitedVenues.add(venue.getId());
            }
        }
        List<PlanItem> items = planner.sequence(ctx, ordered, reasons, true);
        if (items == null) {
                        return null;
        }
        List<String> errors = validator.validate(items, plan.getRequest());
        if (!errors.isEmpty()) {
                        return null;
        }
        items.forEach(i -> i.setVisited(visitedVenues.contains(i.getVenue().getId())));
        return items;
    }

    private PlanningContext baseContext(OutingRequest request, List<Child> kids, Set<Long> avoid) {
        return context(request, kids, avoid, false, request.getArea().getLatitude(), request.getArea().getLongitude(),
                PlanMath.toMinutes(request.getStartTime()), PlanMath.toMinutes(request.getEndTime()), request.getBudget());
    }

    private PlanningContext context(OutingRequest request, List<Child> kids, Set<Long> avoid, boolean tired,
                                    double lat, double lng, int start, int end, int budget) {
        List<Integer> ages = kids.stream().map(Child::getAge).toList();
        Set<Interest> interests = EnumSet.noneOf(Interest.class);
        Set<FoodTag> food = EnumSet.noneOf(FoodTag.class);
        for (Child kid : kids) {
            interests.addAll(kid.getInterests());
            food.addAll(kid.getFoodTags());
        }
        EnergyLevel energy = tired ? EnergyLevel.LOW : effectiveEnergy(request.getMood(), request.getEnergy());
        return new PlanningContext(request, ages, interests, food, lat, lng, start, end, budget, energy, tired, avoid);
    }

    static EnergyLevel effectiveEnergy(Mood mood, EnergyLevel energy) {
        if (energy == null) {
            energy = EnergyLevel.MEDIUM;
        }
        if (mood == null) {
            return energy;
        }
        return switch (mood) {
            case TIRED, CRANKY -> energy.lower();
            case BORED -> energy.higher();
            default -> energy;
        };
    }

    private String defaultTitle(OutingRequest request) {
        String date = request.getOutingDate() == null ? "" : request.getOutingDate().format(DateTimeFormatter.ofPattern("EEE d MMM"));
        return (request.getArea().getLabel() + " day out " + date).trim();
    }

    private String describe(Plan plan, List<Child> kids) {
        OutingRequest r = plan.getRequest();
        if (plan.getItems().isEmpty()) {
            return "No stop fits these limits. Try a larger budget, a longer time window or a wider distance.";
        }
        long activities = plan.getItems().stream().filter(i -> i.getVenue().getCategory() == VenueCategory.ACTIVITY).count();
        return "A " + r.getDurationMinutes() + "-minute plan for " + kids.size()
                + (kids.size() == 1 ? " child" : " children") + " with " + plan.getItems().size() + " stops ("
                + activities + " activit" + (activities == 1 ? "y" : "ies") + "), costing AED " + plan.getTotalCost()
                + " of your AED " + r.getBudget() + " budget.";
    }
}
