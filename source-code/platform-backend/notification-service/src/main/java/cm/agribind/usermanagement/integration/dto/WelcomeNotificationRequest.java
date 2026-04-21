package cm.agribind.usermanagement.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Local copy for notification-service (avoids depending on the full user-management module).
 * Keep fields in sync with user-management when extending the welcome API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WelcomeNotificationRequest {
    private String userId;
    private String userType;
    private String name;
    private String username;
    private String phoneNumber;
    private String email;
    private String temporaryPassword;
    private String registrationNumber;
    private String preferredLanguage;
    private LocalDateTime timestamp;
    private boolean sendSms;
    private boolean sendEmail;
    private String priority;
}
