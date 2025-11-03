package cm.agribind.auth.integration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

// PushNotificationRequest.java (same package)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationRequest {
    private String userId;
    private String title;
    private String body;
    private String type;
    private Map<String, String> data;
}
