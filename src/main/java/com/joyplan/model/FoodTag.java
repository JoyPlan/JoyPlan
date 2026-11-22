package com.joyplan.model;

public enum FoodTag {
    ARABIC("Arabic"),
    ITALIAN("Italian"),
    ASIAN("Asian"),
    BURGERS("Burgers"),
    HEALTHY("Healthy"),
    VEGETARIAN("Vegetarian"),
    DESSERTS("Desserts");

    private final String label;

    FoodTag(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
