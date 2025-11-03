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
    private String userId;
    private String username;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private String role;
    private String cooperativeId;
    private String cooperativeName;
    private String registrationNumber;
    private String preferredLanguage;
    private Boolean firstLogin;
    private Boolean accountLocked;
    private Boolean accountEnabled;
    private Integer failedLoginAttempts;
}
