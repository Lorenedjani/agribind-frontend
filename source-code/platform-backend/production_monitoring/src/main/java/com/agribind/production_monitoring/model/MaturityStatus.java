package com.agribind.production_monitoring.model;

public enum MaturityStatus {
    IMMATURE("Immature"),
    MATURE("Mature"),
    READY_FOR_HARVEST("Ready for Harvest"),
    HARVESTED("Harvested");

    private final String displayName;

    MaturityStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
