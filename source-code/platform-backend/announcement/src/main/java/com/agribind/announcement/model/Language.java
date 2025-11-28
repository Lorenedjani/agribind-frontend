package com.agribind.announcement.model;

public enum Language {
    FRENCH("Français"),
    ENGLISH("English"),
    FULFULDE("Fulfulde"),
    EWONDO("Ewondo"),
    BAMILEKE("Bamiléké"),
    DUALA("Duala"),
    HAUSA("Hausa"),
    PIDGIN("Pidgin");

    private String displayName;

    Language(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }
}
