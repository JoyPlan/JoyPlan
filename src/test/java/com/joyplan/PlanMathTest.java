package com.joyplan;

import com.joyplan.model.Venue;
import com.joyplan.service.PlanMath;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlanMathTest {

    @Test
    void distanceBetweenSamePointIsZero() {
        assertThat(PlanMath.distanceKm(24.47, 54.32, 24.47, 54.32)).isZero();
    }

    @Test
    void distanceFromCornicheToYasIsAboutThirtyKm() {
        double km = PlanMath.distanceKm(24.4764, 54.3213, 24.4890, 54.6060);
        assertThat(km).isBetween(28.0, 30.5);
    }

    @Test
    void travelTimeIsZeroForVeryShortHopsAndRoundedToFiveMinutes() {
        assertThat(PlanMath.travelMinutes(0.1)).isZero();
        assertThat(PlanMath.travelMinutes(10) % 5).isZero();
        assertThat(PlanMath.travelMinutes(10)).isGreaterThanOrEqualTo(20);
    }

    @Test
    void childrenUnderThreeAreFree() {
        Venue venue = new Venue();
        venue.setPriceAdult(50);
        venue.setPriceChild(30);
        assertThat(PlanMath.familyCost(venue, 2, List.of(2, 5, 8))).isEqualTo(160);
    }

    @Test
    void minutesConversionRoundTrips() {
        assertThat(PlanMath.toTime(PlanMath.toMinutes(LocalTime.of(13, 45)))).isEqualTo(LocalTime.of(13, 45));
    }
}
