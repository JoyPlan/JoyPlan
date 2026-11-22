package com.joyplan.model;

public enum Interest {
    ART("Art & crafts"),
    SCIENCE("Science"),
    ANIMALS("Animals"),
    NATURE("Nature"),
    WATER("Water & beach"),
    SPORTS("Sports"),
    PLAY("Play areas"),
    GAMES("Games"),
    READING("Reading"),
    MUSIC("Music");

    private final String label;

    Interest(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
