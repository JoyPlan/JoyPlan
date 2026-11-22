package com.joyplan.service;

import com.joyplan.model.Venue;

public record ScoredVenue(Venue venue, double score, int cost, double distanceKm, String reason) {

    public String getDistanceLabel() {
        return PlanMath.formatKm(distanceKm);
    }

    public long getScorePercent() {
        return Math.max(0, Math.min(100, Math.round(score)));
    }
}
