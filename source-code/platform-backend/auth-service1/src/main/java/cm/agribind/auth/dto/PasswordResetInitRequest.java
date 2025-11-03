package cm.agribind.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// PasswordResetInitRequest.java (same package)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetInitRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}