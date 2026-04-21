package cm.agribind.usermanagement.dto.event;

import cm.agribind.usermanagement.enums.UserType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserCreatedEvent {

    private String eventId;
    private String userId;
    private UserType userType;
    private String name;
    private String phoneNumber;
    private String email;
    private String region;
    private LocalDateTime createdAt;
    private String preferredLanguage;

    // ✅ NEW: Add password field (will be encrypted in transit)
    private String temporaryPassword;
    private boolean requiresPasswordChange;

    // Additional context
    private String source = "USER_MANAGEMENT_SERVICE";
    private LocalDateTime eventTime = LocalDateTime.now();

    public UserCreatedEvent(String userId, UserType userType, String name, String phoneNumber) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.userId = userId;
        this.userType = userType;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.eventTime = LocalDateTime.now();
        this.requiresPasswordChange = true;
    }
}