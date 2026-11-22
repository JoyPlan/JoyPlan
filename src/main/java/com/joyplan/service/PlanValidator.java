package com.joyplan.service;

import com.joyplan.model.OutingRequest;
import com.joyplan.model.PlanItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PlanValidator {

    public List<String> validate(List<PlanItem> items, OutingRequest request) {
        List<String> issues = new ArrayList<>();
        int total = items.stream().mapToInt(PlanItem::getCost).sum();
        if (total > request.getBudget()) {
            issues.add("Total cost AED " + total + " is over the budget of AED " + request.getBudget() + ".");
        }
        int windowStart = PlanMath.toMinutes(request.getStartTime());
        int windowEnd = PlanMath.toMinutes(request.getEndTime());
        int previousEnd = windowStart;
        for (PlanItem item : items) {
            int start = PlanMath.toMinutes(item.getStartTime());
            int end = PlanMath.toMinutes(item.getEndTime());
            String name = item.getVenue().getName();
            if (start < windowStart) {
                issues.add(name + " starts before the time window.");
            }
            if (end > windowEnd) {
                issues.add(name + " ends after the time window.");
            }
            if (end <= start) {
                issues.add(name + " has an invalid time slot.");
            }
            if (start < previousEnd) {
                issues.add(name + " overlaps the previous stop.");
            }
            if (!PlanMath.isOpen(item.getVenue(), start, end)) {
                issues.add(name + " is closed during its time slot.");
            }
            previousEnd = end;
        }
        return issues;
    }

    public boolean isValid(List<PlanItem> items, OutingRequest request) {
        return validate(items, request).isEmpty();
    }
}
