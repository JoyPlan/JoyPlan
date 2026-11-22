package com.joyplan;

import com.joyplan.model.Area;
import com.joyplan.model.EnergyLevel;
import com.joyplan.model.FoodTag;
import com.joyplan.model.Interest;
import com.joyplan.model.Mood;
import com.joyplan.model.OutingRequest;
import com.joyplan.model.Setting;
import com.joyplan.model.SettingPreference;
import com.joyplan.model.VenueCategory;
import com.joyplan.service.PlanMath;
import com.joyplan.service.PlanningContext;
import com.joyplan.service.RecommendationService;
import com.joyplan.service.ScoredVenue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RecommendationServiceTest {

    @Autowired
    private RecommendationService recommendations;

    private PlanningContext context(SettingPreference setting, int budget, int distance, List<Integer> ages, boolean tired) {
        OutingRequest r = new OutingRequest();
        r.setArea(Area.AL_REEM);
        r.setMood(Mood.HAPPY);
        r.setEnergy(EnergyLevel.MEDIUM);
        r.setBudget(budget);
        r.setMaxDistanceKm(distance);
        r.setStartTime(LocalTime.of(10, 0));
        r.setEndTime(LocalTime.of(16, 0));
        r.setOutingDate(LocalDate.now());
        r.setSetting(setting);
        r.setAdults(2);
        r.setIncludeMeal(true);
        return new PlanningContext(r, ages, EnumSet.of(Interest.ART, Interest.SCIENCE), EnumSet.of(FoodTag.HEALTHY),
                Area.AL_REEM.getLatitude(), Area.AL_REEM.getLongitude(), 600, 960, budget,
                tired ? EnergyLevel.LOW : EnergyLevel.MEDIUM, tired, Set.of());
    }

    @Test
    void ranksOnlySuitableVenuesWithinLimits() {
        PlanningContext ctx = context(SettingPreference.ANY, 300, 15, List.of(4, 7), false);
        List<ScoredVenue> ranked = recommendations.rank(ctx, VenueCategory.ACTIVITY);
        assertThat(ranked).isNotEmpty();
        assertThat(ranked).allSatisfy(s -> {
            assertThat(s.distanceKm()).isLessThanOrEqualTo(15);
            assertThat(s.cost()).isLessThanOrEqualTo(300);
            assertThat(s.reason()).isNotBlank();
        });
        for (int i = 1; i < ranked.size(); i++) {
            assertThat(ranked.get(i - 1).score()).isGreaterThanOrEqualTo(ranked.get(i).score());
        }
    }

    @Test
    void indoorPreferenceExcludesOutdoorVenues() {
        PlanningContext ctx = context(SettingPreference.INDOOR, 500, 30, List.of(6), false);
        assertThat(recommendations.rank(ctx, VenueCategory.ACTIVITY))
                .allMatch(s -> s.venue().getSetting() == Setting.INDOOR);
    }

    @Test
    void venuesThatFitNoChildAreExcluded() {
        PlanningContext ctx = context(SettingPreference.ANY, 1000, 60, List.of(1), false);
        assertThat(recommendations.rank(ctx, VenueCategory.ACTIVITY))
                .allMatch(s -> s.venue().getMinAge() <= 1 && s.venue().getMaxAge() >= 1);
    }

    @Test
    void tiredModeExcludesHighEnergyActivities() {
        PlanningContext ctx = context(SettingPreference.ANY, 1000, 60, List.of(8), true);
        assertThat(recommendations.rank(ctx, VenueCategory.ACTIVITY))
                .noneMatch(s -> s.venue().getEnergyLevel() == EnergyLevel.HIGH);
    }

    @Test
    void sponsoredListingsAreLabelledInTheReason() {
        PlanningContext ctx = context(SettingPreference.ANY, 1000, 60, List.of(8), false);
        assertThat(recommendations.rank(ctx, VenueCategory.ACTIVITY))
                .filteredOn(s -> s.venue().isSponsored())
                .allMatch(s -> s.reason().contains("Sponsored"));
    }

    @Test
    void openingHoursAreRespected() {
        PlanningContext ctx = context(SettingPreference.ANY, 1000, 60, List.of(8), false);
        assertThat(recommendations.rank(ctx, VenueCategory.ACTIVITY)).allSatisfy(s -> {
            int open = Math.max(PlanMath.toMinutes(s.venue().getOpenTime()), 600);
            int close = Math.min(PlanMath.toMinutes(s.venue().getCloseTime()), 960);
            assertThat(close - open).isGreaterThanOrEqualTo(s.venue().getDurationMinutes());
        });
    }
}
