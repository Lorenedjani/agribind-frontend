package cm.agribind.usermanagement.dto.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProfileUpdatedEvent {

    private String eventId;
    private String userId;
    private String userType;
    private String updatedFields;
    private String previousLanguage;
    private String newLanguage;
    private Boolean profilePictureChanged;
    private LocalDateTime updatedAt;

    private String source = "USER_MANAGEMENT_SERVICE";
    private LocalDateTime eventTime = LocalDateTime.now();

    public ProfileUpdatedEvent(String userId, String userType) {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.userId = userId;
        this.userType = userType;
        this.eventTime = LocalDateTime.now();
    }
}