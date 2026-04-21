package cm.agribind.auth.integration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// SmsRequest.java (same package)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest {
    private String phoneNumber;
    private String message;
    private String priority;
    private String type;
}
