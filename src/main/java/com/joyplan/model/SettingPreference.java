package com.joyplan.model;

public enum SettingPreference {
    ANY("No preference"),
    INDOOR("Indoor only"),
    OUTDOOR("Outdoor only");

    private final String label;

    SettingPreference(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean allows(Setting setting) {
        return this == ANY || name().equals(setting.name());
    }
}
