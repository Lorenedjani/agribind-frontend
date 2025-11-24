package cm.agribind.usermanagement.integration.dto;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
public class WelcomeNotificationRequest {
    private String userId;
    private String userType;
    private String name;
    private String username;
    private String phoneNumber;
    private String email;
    private String temporaryPassword;
    private String preferredLanguage;
    private LocalDateTime timestamp;

    // Notification preferences
    private boolean sendSms;
    private boolean sendEmail;
    private String priority; // HIGH, MEDIUM, LOW
}