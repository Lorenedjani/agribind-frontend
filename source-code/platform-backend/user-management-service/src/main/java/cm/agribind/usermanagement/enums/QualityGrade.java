package cm.agribind.usermanagement.enums;

public enum QualityGrade {
    GRADE_A("Grade A", "Premium Quality"),
    GRADE_B("Grade B", "Standard Quality"),
    GRADE_C("Grade C", "Basic Quality");

    private final String displayName;
    private final String description;

    QualityGrade(String displayName, String description) {
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