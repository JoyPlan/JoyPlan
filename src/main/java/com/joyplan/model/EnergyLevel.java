package com.joyplan.model;

public enum EnergyLevel {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    private final String label;

    EnergyLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public EnergyLevel lower() {
        return this == HIGH ? MEDIUM : LOW;
    }

    public EnergyLevel higher() {
        return this == LOW ? MEDIUM : HIGH;
    }
}
