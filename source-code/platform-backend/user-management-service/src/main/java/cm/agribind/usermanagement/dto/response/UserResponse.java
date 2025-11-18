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

    // Quick actions availability
    private Boolean canGenerateQR = true;
    private Boolean canExport = true;
    private Boolean canEdit = true;

    // Type-specific responses (populated based on type)
    private FarmerResponse farmerDetails;
    private CooperativeResponse cooperativeDetails;
    private GovernmentResponse governmentDetails;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private String passwordHash;

    // Account status fields for auth
    private Boolean accountLocked = false;
    private Boolean accountEnabled = true;
    private Integer failedLoginAttempts = 0;
    private Boolean firstLogin = false;
}