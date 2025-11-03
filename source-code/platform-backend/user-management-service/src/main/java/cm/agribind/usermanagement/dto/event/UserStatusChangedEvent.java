package cm.agribind.usermanagement.dto.event;

import cm.agribind.usermanagement.enums.UserStatus;
import cm.agribind.usermanagement.enums.UserType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserStatusChangedEvent {

    private String eventId;
    private String userId;
    private UserType userType;
    private String name;
    private UserStatus previousStatus;
    private UserStatus newStatus;
    private String reason;
    private String changedBy;
    private LocalDateTime changedAt;

    private String source = "USER_MANAGEMENT_SERVICE";
    private LocalDateTime eventTime = LocalDateTime.now();

    public UserStatusChangedEvent(String userId, UserType userType, String name,
                                  UserStatus previousStatus, UserStatus newStatus) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.userId = userId;
        this.userType = userType;
        this.name = name;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.eventTime = LocalDateTime.now();
    }
}