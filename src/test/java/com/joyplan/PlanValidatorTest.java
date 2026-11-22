package com.joyplan;

import com.joyplan.model.OutingRequest;
import com.joyplan.model.PlanItem;
import com.joyplan.model.Venue;
import com.joyplan.service.PlanValidator;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlanValidatorTest {

    private final PlanValidator validator = new PlanValidator();

    private Venue venue(String name) {
        Venue v = new Venue();
        v.setName(name);
        v.setOpenTime(LocalTime.of(9, 0));
        v.setCloseTime(LocalTime.of(22, 0));
        return v;
    }

    private OutingRequest request(int budget) {
        OutingRequest r = new OutingRequest();
        r.setBudget(budget);
        r.setStartTime(LocalTime.of(10, 0));
        r.setEndTime(LocalTime.of(12, 0));
        return r;
    }

    @Test
    void validPlanHasNoIssues() {
        List<PlanItem> items = List.of(
                new PlanItem(venue("A"), LocalTime.of(10, 0), LocalTime.of(11, 0), 0, 100, "r"),
                new PlanItem(venue("B"), LocalTime.of(11, 10), LocalTime.of(11, 40), 10, 50, "r"));
        assertThat(validator.validate(items, request(200))).isEmpty();
    }

    @Test
    void detectsOverBudget() {
        List<PlanItem> items = List.of(new PlanItem(venue("A"), LocalTime.of(10, 0), LocalTime.of(11, 0), 0, 300, "r"));
        assertThat(validator.validate(items, request(200))).anyMatch(s -> s.contains("over the budget"));
    }

    @Test
    void detectsStopOutsideTimeWindowAndOverlap() {
        List<PlanItem> items = List.of(
                new PlanItem(venue("A"), LocalTime.of(10, 0), LocalTime.of(11, 30), 0, 0, "r"),
                new PlanItem(venue("B"), LocalTime.of(11, 0), LocalTime.of(12, 30), 0, 0, "r"));
        List<String> issues = validator.validate(items, request(200));
        assertThat(issues).anyMatch(s -> s.contains("after the time window"));
        assertThat(issues).anyMatch(s -> s.contains("overlaps"));
    }

    @Test
    void detectsClosedVenue() {
        Venue late = venue("Late");
        late.setOpenTime(LocalTime.of(15, 0));
        List<PlanItem> items = List.of(new PlanItem(late, LocalTime.of(10, 0), LocalTime.of(11, 0), 0, 0, "r"));
        assertThat(validator.validate(items, request(200))).anyMatch(s -> s.contains("closed"));
    }
}
