package com.joyplan.model;

public enum VenueCategory {
    ACTIVITY("Activity"),
    RESTAURANT("Meal"),
    SNACK("Snack"),
    BREAK("Break");

    private final String label;

    VenueCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
