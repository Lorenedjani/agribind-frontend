package cm.agribind.auth.dto;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "QR data is required")
    @com.fasterxml.jackson.annotation.JsonProperty("qrData")
    private String qrData;

    private String deviceId;
}

