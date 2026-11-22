package com.joyplan.service;

import com.joyplan.model.EnergyLevel;
import com.joyplan.model.PlanItem;
import com.joyplan.model.Venue;
import com.joyplan.model.VenueCategory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class RulePlanner {

    private static final int MAX_WAIT_FOR_OPENING = 30;
    private static final int MAX_STOPS = 8;

    private final RecommendationService recommendations;

    public RulePlanner(RecommendationService recommendations) {
        this.recommendations = recommendations;
    }

    public List<PlanItem> build(PlanningContext ctx) {
        Map<VenueCategory, List<ScoredVenue>> ranked = new EnumMap<>(VenueCategory.class);
        for (VenueCategory category : VenueCategory.values()) {
            ranked.put(category, recommendations.rank(ctx, category));
        }
        int total = ctx.totalMinutes();
        int energyCap = switch (ctx.energy()) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
        };
        int maxActivities = ctx.tiredMode() ? 1 : Math.max(1, Math.min(energyCap, total / 90));
        boolean mealWanted = ctx.request().isIncludeMeal() && total >= 120 && !ctx.tiredMode();

        State state = new State(ctx.startMinute(), ctx.startLat(), ctx.startLng());
        state.mealDone = !mealWanted;

        for (int step = 0; step < MAX_STOPS; step++) {
            List<VenueCategory> order = nextCategories(ctx, state, maxActivities);
            if (order.isEmpty()) {
                break;
            }
            PlanItem next = null;
            for (VenueCategory category : order) {
                next = pick(ranked.get(category), ctx, state);
                if (next != null) {
                    break;
                }
            }
            if (next == null) {
                break;
            }
            state.accept(next);
        }
        return state.items;
    }

    public List<PlanItem> sequence(PlanningContext ctx, List<Venue> ordered, Map<Long, String> reasons, boolean strict) {
        List<PlanItem> items = new ArrayList<>();
        int t = ctx.startMinute();
        double lat = ctx.startLat();
        double lng = ctx.startLng();
        int spent = 0;
        for (Venue venue : ordered) {
            double hop = PlanMath.distanceKm(lat, lng, venue.getLatitude(), venue.getLongitude());
            int travel = PlanMath.travelMinutes(hop);
            int start = Math.max(t + travel, PlanMath.toMinutes(venue.getOpenTime()));
            int end = start + venue.getDurationMinutes();
            int cost = PlanMath.familyCost(venue, ctx.adults(), ctx.childAges());
            boolean fits = start - (t + travel) <= MAX_WAIT_FOR_OPENING
                    && end <= ctx.endMinute()
                    && end <= PlanMath.toMinutes(venue.getCloseTime())
                    && spent + cost <= ctx.budget();
            if (!fits) {
                if (strict) {
                                        return null;
                }
                continue;
            }
            String reason = reasons.getOrDefault(venue.getId(), "Selected for your family.");
            items.add(new PlanItem(venue, PlanMath.toTime(start), PlanMath.toTime(end), travel, cost, reason));
            spent += cost;
            t = end;
            lat = venue.getLatitude();
            lng = venue.getLongitude();
        }
        return items;
    }

    private List<VenueCategory> nextCategories(PlanningContext ctx, State s, int maxActivities) {
        List<VenueCategory> order = new ArrayList<>();
        int remaining = ctx.endMinute() - s.time;
        int elapsed = s.time - ctx.startMinute();
        if (remaining < 15) {
            return order;
        }
        if (ctx.tiredMode() && s.items.isEmpty()) {
            order.add(VenueCategory.BREAK);
            order.add(VenueCategory.SNACK);
            order.add(VenueCategory.ACTIVITY);
            return order;
        }
        boolean mealTime = isMealHour(s.time);
        boolean needRest = s.last == VenueCategory.ACTIVITY && s.breaks < 2
                && (s.lastEnergy == EnergyLevel.HIGH || ctx.energy() == EnergyLevel.LOW);

        if (!s.mealDone && (s.activities >= 1 || mealTime) && (elapsed >= ctx.totalMinutes() / 3 || mealTime)) {
            order.add(VenueCategory.RESTAURANT);
        }
        if (needRest) {
            order.add(VenueCategory.BREAK);
        }
        if (s.activities < maxActivities) {
            order.add(VenueCategory.ACTIVITY);
        }
        if (!s.snackDone && (s.activities >= 1 || ctx.tiredMode()) && s.last != VenueCategory.SNACK) {
            order.add(VenueCategory.SNACK);
        }
        if (!s.mealDone && s.activities >= 1 && !order.contains(VenueCategory.RESTAURANT)) {
            order.add(VenueCategory.RESTAURANT);
        }
        if (s.activities >= 1 && s.activities < maxActivities && s.last != VenueCategory.BREAK
                && s.breaks < 2 && !order.contains(VenueCategory.BREAK) && remaining >= 75) {
            order.add(VenueCategory.BREAK);
        }
        return order;
    }

    private boolean isMealHour(int minute) {
        return (minute >= 12 * 60 && minute <= 14 * 60) || (minute >= 18 * 60 && minute <= 20 * 60);
    }

    private PlanItem pick(List<ScoredVenue> candidates, PlanningContext ctx, State s) {
        ScoredVenue best = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        int bestStart = 0;
        int bestEnd = 0;
        int bestTravel = 0;
        for (ScoredVenue candidate : candidates) {
            Venue venue = candidate.venue();
            if (s.used.contains(venue.getId())) {
                continue;
            }
            double hop = PlanMath.distanceKm(s.lat, s.lng, venue.getLatitude(), venue.getLongitude());
            int travel = PlanMath.travelMinutes(hop);
            int arrival = s.time + travel;
            int start = Math.max(arrival, PlanMath.toMinutes(venue.getOpenTime()));
            if (start - arrival > MAX_WAIT_FOR_OPENING) {
                continue;
            }
            int end = start + venue.getDurationMinutes();
            if (end > ctx.endMinute() || end > PlanMath.toMinutes(venue.getCloseTime())) {
                continue;
            }
            if (s.spent + candidate.cost() > ctx.budget()) {
                continue;
            }
            double value = candidate.score() - hop * 1.5;
            if (value > bestValue) {
                bestValue = value;
                best = candidate;
                bestStart = start;
                bestEnd = end;
                bestTravel = travel;
            }
        }
        if (best == null) {
            return null;
        }
        return new PlanItem(best.venue(), PlanMath.toTime(bestStart), PlanMath.toTime(bestEnd),
                bestTravel, best.cost(), best.reason());
    }

    private static final class State {
        private final List<PlanItem> items = new ArrayList<>();
        private final Set<Long> used = new HashSet<>();
        private int time;
        private double lat;
        private double lng;
        private int spent;
        private int activities;
        private int breaks;
        private boolean snackDone;
        private boolean mealDone;
        private VenueCategory last;
        private EnergyLevel lastEnergy;

        private State(int time, double lat, double lng) {
            this.time = time;
            this.lat = lat;
            this.lng = lng;
        }

        private void accept(PlanItem item) {
            Venue venue = item.getVenue();
            items.add(item);
            used.add(venue.getId());
            spent += item.getCost();
            time = PlanMath.toMinutes(item.getEndTime());
            lat = venue.getLatitude();
            lng = venue.getLongitude();
            last = venue.getCategory();
            lastEnergy = venue.getEnergyLevel();
            switch (venue.getCategory()) {
                case ACTIVITY -> activities++;
                case BREAK -> breaks++;
                case SNACK -> snackDone = true;
                case RESTAURANT -> mealDone = true;
            }
        }
    }
}
