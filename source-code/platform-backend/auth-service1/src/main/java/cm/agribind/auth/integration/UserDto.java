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
    private String username;  // Email or phone
    private String email;
    private String phoneNumber;
    private String passwordHash;

    // Role and status
    private String role;  // FARMER, COOPERATIVE, GOVERNMENT
    private String status;  // ACTIVE, INACTIVE

    // Cooperative info
    private String cooperativeId;
    private String cooperativeName;

    // Registration
    private String registrationNumber;

    // Preferences
    private String preferredLanguage;
    private Boolean firstLogin;

    // ✅ CRITICAL FIX: Account status fields with proper defaults
    private Boolean accountLocked;
    private Boolean accountEnabled;
    private Integer failedLoginAttempts;

    // ✅ Helper method to safely get account status
    public Boolean getAccountLocked() {
        return accountLocked != null ? accountLocked : false;
    }

    public Boolean getAccountEnabled() {
        return accountEnabled != null ? accountEnabled : true;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts != null ? failedLoginAttempts : 0;
    }

    public Boolean getFirstLogin() {
        return firstLogin != null ? firstLogin : true;
    }

    public String getPreferredLanguage() {
        return preferredLanguage != null ? preferredLanguage : "en";
    }
}