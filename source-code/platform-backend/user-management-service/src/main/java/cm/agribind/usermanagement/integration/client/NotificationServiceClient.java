package cm.agribind.usermanagement.integration.client;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceClient {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RestTemplate restTemplate;

    @Value("${services.notification-service.url}")
    private String notificationServiceUrl;

    private static final String NOTIFICATION_TOPIC = "notifications";

    public void sendSMS(SmsNotificationRequest request) {
        try {
            // Send via Kafka for async processing
            kafkaTemplate.send(NOTIFICATION_TOPIC, request);
            log.info("SMS notification sent to Kafka for: {}", request.getPhoneNumber());
        } catch (Exception e) {
            log.error("Failed to send SMS notification", e);
            // Fallback to REST API
            sendSmsViaRest(request);
        }
    }

    private void sendSmsViaRest(SmsNotificationRequest request) {
        try {
            String url = notificationServiceUrl + "/api/notifications/sms/send";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<SmsNotificationRequest> entity = new HttpEntity<>(request, headers);

            restTemplate.postForObject(url, entity, NotificationResponse.class);
            log.info("SMS sent via REST for: {}", request.getPhoneNumber());
        } catch (Exception e) {
            log.error("Failed to send SMS via REST", e);
        }
    }

    @Data
    public static class SmsNotificationRequest {
        private String userId;
        private String phoneNumber;
        private String message;
        private String type; // WELCOME, PASSWORD_RESET, QR_CODE, etc.
        private String priority = "HIGH";
        private LocalDateTime timestamp = LocalDateTime.now();
    }

    @Data
    public static class NotificationResponse {
        private String notificationId;
        private String status;
        private String message;
    }
}
