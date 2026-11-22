package com.joyplan.service;

import com.joyplan.model.VenueCategory;

import java.util.List;
import java.util.Map;

public record AdminStats(
        long users,
        long venues,
        long activeVenues,
        long staleVenues,
        long plans,
        long savedPlans,
        long completedPlans,
        Double averageRating,
        Map<VenueCategory, Long> venuesByCategory,
        List<Object[]> topVenues) {

    public String getAverageRatingLabel() {
        return averageRating == null ? "–" : String.format(java.util.Locale.ROOT, "%.1f / 5", averageRating);
    }

    public long getMaxCategoryCount() {
        return venuesByCategory.values().stream().mapToLong(Long::longValue).max().orElse(1L) == 0 ? 1L : venuesByCategory.values().stream().mapToLong(Long::longValue).max().orElse(1L);
    }
}
