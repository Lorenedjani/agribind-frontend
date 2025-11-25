package cm.agribind.usermanagement.dto.response;

import cm.agribind.usermanagement.enums.Region;
import cm.agribind.usermanagement.enums.UserStatus;
import cm.agribind.usermanagement.enums.UserType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long id;
    private String userId;
    private UserType type;
    private String name;
    private String email;
    private String phoneNumber;
    private UserStatus status;
    private String registrationNumber;

    // Address information
    private Region region;
    private String department;
    private String district;
    private String village;
    private String fullAddress;
    private String gpsCoordinates;

    // Profile information
    private String profilePictureUrl;
    private String preferredLanguage;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Quick actions
    private Boolean canGenerateQR = true;
    private Boolean canExport = true;
    private Boolean canEdit = true;

    // Type-specific responses
    private FarmerResponse farmerDetails;
    private CooperativeResponse cooperativeDetails;
    private GovernmentResponse governmentDetails;

    // ✅ CRITICAL FIX: Add auth fields required by Auth Service
    private String passwordHash;

    // ✅ Account security fields - with proper initialization
    private Boolean accountLocked;
    private Boolean accountEnabled;
    private Integer failedLoginAttempts;
    private Boolean firstLogin;
    private LocalDateTime lastLoginAt;
    private LocalDateTime lastPasswordChange;

    // ✅ Helper methods for safe access
    public Boolean getAccountLocked() {
        return accountLocked != null ? accountLocked : false;
    }

    public Boolean getAccountEnabled() {
        return accountEnabled != null ? accountEnabled : true;
    }

    public Boolean getFirstLogin() {
        return firstLogin != null ? firstLogin : true;
    }

    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts != null ? failedLoginAttempts : 0;
    }

    // ✅ For Auth Service compatibility
    public String getRole() {
        return type != null ? type.name() : null;
    }

    public String getUsername() {
        return email; // Primary username is email
    }
}