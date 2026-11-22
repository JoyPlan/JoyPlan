package com.joyplan.model;

public enum Mood {
    HAPPY("Happy"),
    EXCITED("Excited"),
    CALM("Calm"),
    BORED("Bored"),
    TIRED("Tired"),
    CRANKY("Cranky");

    private final String label;

    Mood(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
