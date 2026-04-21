package cm.agribind.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

// LoginRequest.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Username/email is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    private String deviceId;
}