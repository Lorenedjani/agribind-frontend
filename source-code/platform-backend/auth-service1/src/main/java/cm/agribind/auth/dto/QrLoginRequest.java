package cm.agribind.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// QrLoginRequest.java (same package)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrLoginRequest {
    @NotBlank(message = "Registration number is required")
    @Pattern(regexp = "^[A-Z0-9]{6,8}$", message = "Invalid registration number format")
    private String registrationNumber;

    private String deviceId;
}

