package com.joyplan.service;

import com.joyplan.model.Venue;

import java.time.LocalTime;
import java.util.List;

public final class PlanMath {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double AVERAGE_SPEED_KMH = 35.0;
    private static final int PARKING_BUFFER_MINUTES = 5;
    private static final int LAST_MINUTE_OF_DAY = 23 * 60 + 59;

    private PlanMath() {
    }

    public static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public static int travelMinutes(double km) {
        if (km < 0.3) {
            return 0;
        }
        int minutes = (int) Math.ceil(km / AVERAGE_SPEED_KMH * 60.0) + PARKING_BUFFER_MINUTES;
        return (int) (Math.ceil(minutes / 5.0) * 5);
    }

    public static int toMinutes(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    public static LocalTime toTime(int minutes) {
        int m = Math.max(0, Math.min(minutes, LAST_MINUTE_OF_DAY));
        return LocalTime.of(m / 60, m % 60);
    }

    public static int familyCost(Venue venue, int adults, List<Integer> childAges) {
        int payingChildren = (int) childAges.stream().filter(age -> age >= 3).count();
        return venue.getPriceAdult() * Math.max(1, adults) + venue.getPriceChild() * payingChildren;
    }

    public static boolean isOpen(Venue venue, int fromMinute, int toMinute) {
        return toMinutes(venue.getOpenTime()) <= fromMinute && toMinute <= toMinutes(venue.getCloseTime());
    }

    public static String formatKm(double km) {
        return String.format(java.util.Locale.ROOT, "%.1f km", km);
    }
}
