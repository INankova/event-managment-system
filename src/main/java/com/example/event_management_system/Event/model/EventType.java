package com.example.event_management_system.Event.model;

import lombok.Getter;

@Getter
public enum EventType {
    SPORTS("Sports"), CONCERTS("Concerts"), THEATER("Theater"), CINEMA("Cinema");

    private final String displayName;

    EventType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
