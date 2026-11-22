package com.joyplan;

import com.joyplan.model.Area;
import com.joyplan.model.EnergyLevel;
import com.joyplan.model.Mood;
import com.joyplan.model.OutingRequest;
import com.joyplan.model.Plan;
import com.joyplan.model.PlanItem;
import com.joyplan.model.PlanStatus;
import com.joyplan.model.SettingPreference;
import com.joyplan.model.User;
import com.joyplan.model.VenueCategory;
import com.joyplan.repository.UserRepository;
import com.joyplan.service.Alternatives;
import com.joyplan.service.PlanMath;
import com.joyplan.service.PlanService;
import com.joyplan.service.PlanValidator;
import com.joyplan.service.ScoredVenue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PlanServiceTest {

    @Autowired
    private PlanService planService;

    @Autowired
    private PlanValidator validator;

    @Autowired
    private UserRepository userRepository;

    private User parent;
    private User partner;
    private User admin;

    @BeforeEach
    void setUp() {
        parent = userRepository.findByEmailIgnoreCase("parent@joyplan.ae").orElseThrow();
        partner = userRepository.findByEmailIgnoreCase("partner@joyplan.ae").orElseThrow();
        admin = userRepository.findByEmailIgnoreCase("admin@joyplan.ae").orElseThrow();
    }

    private OutingRequest request(int budget, LocalTime start, LocalTime end, EnergyLevel energy, Mood mood) {
        OutingRequest r = new OutingRequest();
        r.setArea(Area.AL_REEM);
        r.setMood(mood);
        r.setEnergy(energy);
        r.setBudget(budget);
        r.setMaxDistanceKm(20);
        r.setStartTime(start);
        r.setEndTime(end);
        r.setOutingDate(LocalDate.now());
        r.setSetting(SettingPreference.ANY);
        r.setAdults(2);
        r.setIncludeMeal(true);
        return r;
    }

    @Test
    void generatedPlanStaysWithinBudgetAndTimeWindow() {
        OutingRequest r = request(400, LocalTime.of(10, 0), LocalTime.of(15, 0), EnergyLevel.MEDIUM, Mood.HAPPY);
        Plan plan = planService.generate(parent, r);
        assertThat(plan.getItems()).hasSizeGreaterThanOrEqualTo(3);
        assertThat(plan.getTotalCost()).isLessThanOrEqualTo(400);
        assertThat(validator.validate(plan.getItems(), r)).isEmpty();
        assertThat(plan.getItems()).anyMatch(i -> i.getVenue().getCategory() == VenueCategory.ACTIVITY);
        assertThat(plan.getItems()).anyMatch(i -> i.getVenue().getCategory() == VenueCategory.RESTAURANT);
        assertThat(plan.getItems()).allMatch(i -> i.getReason() != null && !i.getReason().isBlank());
    }

    @Test
    void exampleFromProposalTiredIndoorLimitedBudgetTwoHours() {
        OutingRequest r = request(150, LocalTime.of(16, 0), LocalTime.of(18, 0), EnergyLevel.LOW, Mood.TIRED);
        r.setSetting(SettingPreference.INDOOR);
        Plan plan = planService.generate(parent, r);
        assertThat(plan.getItems()).isNotEmpty();
        assertThat(plan.getTotalCost()).isLessThanOrEqualTo(150);
        assertThat(plan.getItems()).allMatch(i -> i.getVenue().getSetting().name().equals("INDOOR"));
        assertThat(plan.getItems()).noneMatch(i -> i.getVenue().getEnergyLevel() == EnergyLevel.HIGH);
        assertThat(validator.validate(plan.getItems(), r)).isEmpty();
    }

    @Test
    void impossibleConstraintsReturnAnEmptyPlanInsteadOfBreakingLimits() {
        OutingRequest r = request(0, LocalTime.of(10, 0), LocalTime.of(11, 0), EnergyLevel.MEDIUM, Mood.HAPPY);
        r.setMaxDistanceKm(1);
        Plan plan = planService.generate(parent, r);
        assertThat(plan.getTotalCost()).isZero();
        assertThat(validator.validate(plan.getItems(), r)).isEmpty();
    }

    @Test
    void replacingOneItemOffersAtLeastTwoOptionsAndKeepsOtherItems() {
        Plan plan = planService.generate(parent, request(400, LocalTime.of(10, 0), LocalTime.of(15, 0), EnergyLevel.MEDIUM, Mood.HAPPY));
        PlanItem first = plan.getItems().get(0);
        Alternatives alternatives = planService.alternatives(plan.getId(), first.getId(), parent);
        assertThat(alternatives.options()).hasSizeGreaterThanOrEqualTo(2);

        List<Long> othersBefore = plan.getItems().stream().skip(1).map(i -> i.getVenue().getId()).toList();
        ScoredVenue choice = alternatives.options().get(0);
        Plan updated = planService.replace(plan.getId(), first.getId(), choice.venue().getId(), parent);

        assertThat(updated.getItems().get(0).getVenue().getId()).isEqualTo(choice.venue().getId());
        assertThat(updated.getItems().stream().skip(1).map(i -> i.getVenue().getId()).toList()).isEqualTo(othersBefore);
        assertThat(updated.getTotalCost()).isLessThanOrEqualTo(400);
        assertThat(validator.validate(updated.getItems(), updated.getRequest())).isEmpty();
    }

    @Test
    void kidsAreTiredShortensTheRestOfTheDayWithCalmStops() {
        Plan plan = planService.generate(parent, request(500, LocalTime.of(10, 0), LocalTime.of(17, 0), EnergyLevel.HIGH, Mood.EXCITED));
        PlanItem first = plan.getItems().get(0);
        planService.toggleVisited(plan.getId(), first.getId(), parent);
        int resumeAt = PlanMath.toMinutes(first.getEndTime());
        Long firstVenue = first.getVenue().getId();

        String message = planService.kidsAreTired(plan.getId(), parent);

        assertThat(message).isNotBlank();
        assertThat(plan.isTiredAdjusted()).isTrue();
        assertThat(plan.getItems().get(0).getVenue().getId()).isEqualTo(firstVenue);
        assertThat(plan.getItems().get(0).isVisited()).isTrue();
        plan.getItems().stream().skip(1).forEach(item -> {
            assertThat(PlanMath.toMinutes(item.getEndTime())).isLessThanOrEqualTo(resumeAt + 90);
            assertThat(item.getVenue().getEnergyLevel()).isNotEqualTo(EnergyLevel.HIGH);
        });
    }

    @Test
    void kidsAreTiredWhenAllStopsAreDoneEndsTheDay() {
        Plan plan = planService.generate(parent, request(400, LocalTime.of(10, 0), LocalTime.of(13, 0), EnergyLevel.MEDIUM, Mood.HAPPY));
        new java.util.ArrayList<>(plan.getItems()).forEach(i -> planService.toggleVisited(plan.getId(), i.getId(), parent));
        assertThat(planService.kidsAreTired(plan.getId(), parent)).contains("day is over");
    }

    @Test
    void planChangesCompleteWithinFiveSeconds() {
        Plan plan = planService.generate(parent, request(400, LocalTime.of(10, 0), LocalTime.of(15, 0), EnergyLevel.MEDIUM, Mood.HAPPY));
        long start = System.nanoTime();
        planService.regenerate(plan.getId(), parent);
        planService.alternatives(plan.getId(), plan.getItems().get(0).getId(), parent);
        planService.kidsAreTired(plan.getId(), parent);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertThat(elapsedMs).isLessThan(5000);
    }

    @Test
    void saveRateReuseAndShareWork() {
        Plan plan = planService.generate(parent, request(400, LocalTime.of(10, 0), LocalTime.of(15, 0), EnergyLevel.MEDIUM, Mood.HAPPY));
        planService.save(plan.getId(), parent);
        assertThat(plan.getStatus()).isEqualTo(PlanStatus.SAVED);

        planService.rate(plan.getId(), 5, "Great day", parent);
        assertThat(plan.getRating()).isEqualTo(5);
        assertThat(plan.getStatus()).isEqualTo(PlanStatus.COMPLETED);

        Plan reused = planService.reuse(plan.getId(), parent);
        assertThat(reused.getId()).isNotEqualTo(plan.getId());
        assertThat(reused.getRequest().getBudget()).isEqualTo(400);

        planService.share(plan.getId(), "partner@joyplan.ae", parent);
        assertThat(planService.accessible(plan.getId(), partner)).isNotNull();
        assertThat(planService.sharedWith(partner)).extracting(Plan::getId).contains(plan.getId());
    }

    @Test
    void onlyOwnerCanChangeAPlanAndStrangersCannotView() {
        Plan plan = planService.generate(parent, request(400, LocalTime.of(10, 0), LocalTime.of(15, 0), EnergyLevel.MEDIUM, Mood.HAPPY));
        assertThatThrownBy(() -> planService.accessible(plan.getId(), admin)).isInstanceOf(AccessDeniedException.class);
        planService.share(plan.getId(), "partner@joyplan.ae", parent);
        assertThatThrownBy(() -> planService.regenerate(plan.getId(), partner)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void generatingWithoutChildrenIsRejected() {
        OutingRequest r = request(400, LocalTime.of(10, 0), LocalTime.of(15, 0), EnergyLevel.MEDIUM, Mood.HAPPY);
        assertThatThrownBy(() -> planService.generate(partner, r)).isInstanceOf(IllegalStateException.class);
    }
}
