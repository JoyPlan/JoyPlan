package com.joyplan.model;

public enum PlanStatus {
    DRAFT("Draft"),
    SAVED("Saved"),
    COMPLETED("Completed");

    private final String label;

    PlanStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
