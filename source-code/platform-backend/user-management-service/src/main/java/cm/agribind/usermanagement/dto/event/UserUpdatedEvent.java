package cm.agribind.usermanagement.dto.event;

import cm.agribind.usermanagement.enums.UserStatus;
import cm.agribind.usermanagement.enums.UserType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserUpdatedEvent {

    private String eventId;
    private String userId;
    private UserType userType;
    private String name;
    private UserStatus previousStatus;
    private UserStatus newStatus;
    private String updatedFields;
    private LocalDateTime updatedAt;
    private String updatedBy;

    private String source = "USER_MANAGEMENT_SERVICE";
    private LocalDateTime eventTime = LocalDateTime.now();

    public UserUpdatedEvent(String userId, UserType userType, String name) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.userId = userId;
        this.userType = userType;
        this.name = name;
        this.eventTime = LocalDateTime.now();
    }
}