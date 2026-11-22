package com.joyplan.model;

public enum Setting {
    INDOOR("Indoor"),
    OUTDOOR("Outdoor");

    private final String label;

    Setting(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
