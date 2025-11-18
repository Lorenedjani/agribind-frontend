package cm.agribind.auth.integration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    // Basic user info
    private String userId;
    private String username;  // This will be email
    private String email;
    private String phoneNumber;
    private String passwordHash;

    // Role and status
    private String role;  // FARMER, COOPERATIVE, GOVERNMENT
    private String status;  // ACTIVE, INACTIVE, etc.

    // Cooperative info
    private String cooperativeId;
    private String cooperativeName;

    // Registration
    private String registrationNumber;

    // Preferences
    private String preferredLanguage;
    private Boolean firstLogin;

    // Account status
    private Boolean accountLocked;
    private Boolean accountEnabled;
    private Integer failedLoginAttempts;
}