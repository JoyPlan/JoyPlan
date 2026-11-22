package com.joyplan.service;

import com.joyplan.model.EnergyLevel;
import com.joyplan.model.FoodTag;
import com.joyplan.model.Interest;
import com.joyplan.model.OutingRequest;

import java.util.List;
import java.util.Set;

public record PlanningContext(
        OutingRequest request,
        List<Integer> childAges,
        Set<Interest> interests,
        Set<FoodTag> foodTags,
        double startLat,
        double startLng,
        int startMinute,
        int endMinute,
        int budget,
        EnergyLevel energy,
        boolean tiredMode,
        Set<Long> avoidVenueIds) {

    public int adults() {
        return Math.max(1, request.getAdults());
    }

    public int totalMinutes() {
        return endMinute - startMinute;
    }
}
