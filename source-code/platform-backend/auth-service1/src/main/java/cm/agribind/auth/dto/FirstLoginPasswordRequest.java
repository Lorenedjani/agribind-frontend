package cm.agribind.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// FirstLoginPasswordRequest.java (same package)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirstLoginPasswordRequest {
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Password must contain uppercase, lowercase and number")
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    private String preferredLanguage;
}
