package com.joyplan.service;

import com.joyplan.model.EnergyLevel;
import com.joyplan.model.FoodTag;
import com.joyplan.model.Interest;
import com.joyplan.model.Venue;
import com.joyplan.model.VenueCategory;
import com.joyplan.repository.VenueRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final VenueRepository venueRepository;

    public RecommendationService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public List<ScoredVenue> rank(PlanningContext ctx, VenueCategory category) {
        return rank(ctx, category, ctx.request().getMaxDistanceKm());
    }

    public List<ScoredVenue> rank(PlanningContext ctx, VenueCategory category, double maxDistanceKm) {
        List<ScoredVenue> result = new ArrayList<>();
        for (Venue venue : venueRepository.findByActiveTrueAndCategory(category)) {
            ScoredVenue scored = score(ctx, venue, maxDistanceKm);
            if (scored != null) {
                result.add(scored);
            }
        }
        result.sort(Comparator.comparingDouble(ScoredVenue::score).reversed()
                .thenComparingDouble(ScoredVenue::distanceKm));
        return result;
    }

    public ScoredVenue score(PlanningContext ctx, Venue venue, double maxDistanceKm) {
        double distance = PlanMath.distanceKm(ctx.startLat(), ctx.startLng(), venue.getLatitude(), venue.getLongitude());
        if (distance > maxDistanceKm) {
            return null;
        }
        if (ctx.request().getSetting() != null && !ctx.request().getSetting().allows(venue.getSetting())) {
            return null;
        }
        double ageFit = ageFit(venue, ctx.childAges());
        if (ageFit == 0) {
            return null;
        }
        int overlap = Math.min(PlanMath.toMinutes(venue.getCloseTime()), ctx.endMinute())
                - Math.max(PlanMath.toMinutes(venue.getOpenTime()), ctx.startMinute());
        if (overlap < venue.getDurationMinutes()) {
            return null;
        }
        int cost = PlanMath.familyCost(venue, ctx.adults(), ctx.childAges());
        if (cost > ctx.budget()) {
            return null;
        }
        if ((ctx.tiredMode() || ctx.energy() == EnergyLevel.LOW) && venue.getCategory() == VenueCategory.ACTIVITY
                && venue.getEnergyLevel() == EnergyLevel.HIGH) {
            return null;
        }

        double distanceScore = maxDistanceKm <= 0 ? 1 : 1 - distance / maxDistanceKm;
        double costScore = ctx.budget() <= 0 ? (cost == 0 ? 1 : 0) : 1 - (double) cost / ctx.budget();
        Set<Interest> matchedInterests = matched(venue.getInterests(), ctx.interests());
        Set<FoodTag> matchedFood = matchedFood(venue.getFoodTags(), ctx.foodTags());

        double score;
        switch (venue.getCategory()) {
            case ACTIVITY -> {
                double interestScore = venue.getInterests().isEmpty() ? 0.4
                        : matchedInterests.isEmpty() ? 0 : 0.5 + 0.5 * Math.min(1.0, matchedInterests.size() / 2.0);
                double energyScore = energyScore(venue.getEnergyLevel(), ctx.energy());
                score = interestScore * 30 + ageFit * 25 + energyScore * 20 + distanceScore * 15 + costScore * 10;
            }
            case RESTAURANT, SNACK -> {
                double foodScore = foodScore(venue.getFoodTags(), ctx.foodTags(), matchedFood);
                score = foodScore * 35 + ageFit * 15 + distanceScore * 30 + costScore * 20;
            }
            default -> score = distanceScore * 70 + ageFit * 30;
        }
        if (ctx.avoidVenueIds() != null && ctx.avoidVenueIds().contains(venue.getId())) {
            score -= 25;
        }
        String reason = reason(ctx, venue, ageFit, distance, cost, matchedInterests, matchedFood);
        return new ScoredVenue(venue, score, cost, distance, reason);
    }

    private double ageFit(Venue venue, List<Integer> ages) {
        if (ages.isEmpty()) {
            return 1;
        }
        long fitting = ages.stream().filter(a -> a >= venue.getMinAge() && a <= venue.getMaxAge()).count();
        return (double) fitting / ages.size();
    }

    private double energyScore(EnergyLevel venueEnergy, EnergyLevel familyEnergy) {
        int diff = Math.abs(venueEnergy.ordinal() - familyEnergy.ordinal());
        return diff == 0 ? 1 : diff == 1 ? 0.5 : 0;
    }

    private double foodScore(Set<FoodTag> venueTags, Set<FoodTag> wanted, Set<FoodTag> matched) {
        if (wanted.contains(FoodTag.VEGETARIAN) && !venueTags.contains(FoodTag.VEGETARIAN)) {
            return 0.1;
        }
        if (wanted.isEmpty()) {
            return 0.6;
        }
        return matched.isEmpty() ? 0.2 : 0.6 + 0.4 * Math.min(1.0, matched.size() / 2.0);
    }

    private Set<Interest> matched(Set<Interest> venueInterests, Set<Interest> wanted) {
        Set<Interest> result = EnumSet.noneOf(Interest.class);
        for (Interest i : venueInterests) {
            if (wanted.contains(i)) {
                result.add(i);
            }
        }
        return result;
    }

    private Set<FoodTag> matchedFood(Set<FoodTag> venueTags, Set<FoodTag> wanted) {
        Set<FoodTag> result = EnumSet.noneOf(FoodTag.class);
        for (FoodTag t : venueTags) {
            if (wanted.contains(t)) {
                result.add(t);
            }
        }
        return result;
    }

    private String reason(PlanningContext ctx, Venue venue, double ageFit, double distance, int cost,
                          Set<Interest> interests, Set<FoodTag> food) {
        List<String> parts = new ArrayList<>();
        switch (venue.getCategory()) {
            case ACTIVITY -> {
                if (!interests.isEmpty()) {
                    parts.add("Matches your children's interests: "
                            + interests.stream().map(Interest::getLabel).collect(Collectors.joining(", ")));
                }
                parts.add(ageFit == 1 ? "Suits all your children's ages (" + venue.getAgeRange() + ")"
                        : "Suits some of your children (" + venue.getAgeRange() + ")");
                parts.add(venue.getEnergyLevel().getLabel() + "-energy activity for a "
                        + ctx.energy().getLabel().toLowerCase() + "-energy day");
            }
            case RESTAURANT, SNACK -> {
                if (!food.isEmpty()) {
                    parts.add("Serves food your family likes: "
                            + food.stream().map(FoodTag::getLabel).collect(Collectors.joining(", ")));
                } else {
                    parts.add("Family-friendly " + venue.getCategory().getLabel().toLowerCase() + " stop");
                }
            }
            default -> parts.add("A short rest to recharge before the next stop");
        }
        parts.add(PlanMath.formatKm(distance) + " from your start point");
        parts.add(cost == 0 ? "Free" : "About AED " + cost + " for the family");
        String text = String.join(" · ", parts) + ".";
        if (venue.isSponsored()) {
            text += " Sponsored listing: ranked by suitability only.";
        }
        return text;
    }
}
