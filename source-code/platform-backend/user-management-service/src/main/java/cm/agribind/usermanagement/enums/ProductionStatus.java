package cm.agribind.usermanagement.enums;

public enum ProductionStatus {
    PENDING("Pending", "Awaiting verification"),
    VERIFIED("Verified", "Quality verified and accepted"),
    REJECTED("Rejected", "Quality did not meet standards"),
    PROCESSED("Processed", "Processed and stored"),
    SOLD("Sold", "Sold to buyer");

    private final String displayName;
    private final String description;

    ProductionStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}