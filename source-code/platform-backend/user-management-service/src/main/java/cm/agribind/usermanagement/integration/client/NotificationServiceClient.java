package cm.agribind.usermanagement.integration.client;

import cm.agribind.usermanagement.integration.dto.WelcomeNotificationRequest;
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

    @Value("${services.notification-service.url:http://localhost:8083}")
    private String notificationServiceUrl;

    @Value("${agribind.notifications.use-kafka:false}")
    private boolean useKafka;

    private static final String NOTIFICATION_TOPIC = "notifications";

    /**
     * Send welcome notification with credentials via SMS and Email
     */
    public void sendWelcomeNotification(WelcomeNotificationRequest request) {
        log.info("Sending welcome notification to user: {} (Type: {})",
                request.getUserId(), request.getUserType());

        // ✅ CHANGED: Skip Kafka if not configured
        if (useKafka) {
            try {
                kafkaTemplate.send(NOTIFICATION_TOPIC, "user.welcome", request);
                log.info("Welcome notification queued via Kafka for: {}", request.getUserId());
                return; // Success - exit
            } catch (Exception e) {
                log.warn("Kafka failed, falling back to REST: {}", e.getMessage());
            }
        }

        // ✅ Use REST API directly
        sendWelcomeViaRest(request);
    }

    /**
     * Send SMS notification
     */
    public void sendSMS(SmsNotificationRequest request) {
        if (useKafka) {
            try {
                kafkaTemplate.send(NOTIFICATION_TOPIC, "sms.send", request);
                log.info("SMS notification sent to Kafka for: {}", request.getPhoneNumber());
                return;
            } catch (Exception e) {
                log.warn("Kafka failed for SMS, using REST: {}", e.getMessage());
            }
        }
        sendSmsViaRest(request);
    }

    /**
     * Send email notification
     */
    public void sendEmail(EmailNotificationRequest request) {
        if (useKafka) {
            try {
                kafkaTemplate.send(NOTIFICATION_TOPIC, "email.send", request);
                log.info("Email notification sent to Kafka for: {}", request.getEmail());
                return;
            } catch (Exception e) {
                log.warn("Kafka failed for Email, using REST: {}", e.getMessage());
            }
        }
        sendEmailViaRest(request);
    }

    /**
     * Send welcome credentials via SMS - UPDATED METHOD SIGNATURE
     */
    public void sendWelcomeSMS(String phoneNumber, String name, String username, String password, String userType) {
        SmsNotificationRequest request = new SmsNotificationRequest();
        request.setPhoneNumber(phoneNumber);
        request.setMessage(buildWelcomeSmsMessage(name, username, password, userType));
        request.setType("WELCOME_CREDENTIALS");
        request.setPriority("HIGH");
        request.setTimestamp(LocalDateTime.now());

        sendSMS(request);
    }

    /**
     * Send welcome credentials via Email
     */
    public void sendWelcomeEmail(String email, String name, String username,
                                 String password, String userType) {
        if (email == null || email.trim().isEmpty()) {
            log.warn("No email provided, skipping email notification");
            return;
        }

        EmailNotificationRequest request = new EmailNotificationRequest();
        request.setEmail(email);
        request.setSubject("Welcome to AgriBind - Your Login Credentials");
        request.setBody(buildWelcomeEmailBody(name, username, password, userType));
        request.setType("WELCOME_CREDENTIALS");
        request.setPriority("HIGH");
        request.setTimestamp(LocalDateTime.now());

        sendEmail(request);
    }

    // ===== PRIVATE HELPER METHODS =====

    private String buildWelcomeSmsMessage(String name, String username, String password, String userType) {
        String role = formatUserType(userType);

        return String.format(
                "Welcome to AgriBind, %s!\n\n" +
                        "Your %s account has been created.\n\n" +
                        "Login Details:\n" +
                        "Username: %s\n" +
                        "Password: %s\n\n" +
                        "⚠️ Please change your password on first login.\n\n" +
                        "Download the app: https://agribind.cm/app\n\n" +
                        "Need help? Call: +237 XXX XXX XXX",
                name, role, username, password
        );
    }

    private String buildWelcomeEmailBody(String name, String username, String password, String userType) {
        String role = formatUserType(userType);

        return String.format(
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <style>\n" +
                        "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }\n" +
                        "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
                        "        .header { background-color: #2e7d32; color: white; padding: 20px; text-align: center; }\n" +
                        "        .content { background-color: #f5f5f5; padding: 30px; }\n" +
                        "        .credentials { background-color: white; padding: 20px; border-left: 4px solid #2e7d32; margin: 20px 0; }\n" +
                        "        .warning { background-color: #fff3cd; padding: 15px; border-left: 4px solid #ffc107; margin: 20px 0; }\n" +
                        "        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }\n" +
                        "        .button { background-color: #2e7d32; color: white; padding: 12px 30px; text-decoration: none; display: inline-block; border-radius: 5px; margin: 20px 0; }\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <div class='container'>\n" +
                        "        <div class='header'>\n" +
                        "            <h1>🌾 Welcome to AgriBind!</h1>\n" +
                        "        </div>\n" +
                        "        <div class='content'>\n" +
                        "            <h2>Dear %s,</h2>\n" +
                        "            <p>Your <strong>%s account</strong> has been successfully created on the AgriBind platform.</p>\n" +
                        "            \n" +
                        "            <div class='credentials'>\n" +
                        "                <h3>🔐 Your Login Credentials:</h3>\n" +
                        "                <p><strong>Username:</strong> %s</p>\n" +
                        "                <p><strong>Temporary Password:</strong> <code style='background: #f0f0f0; padding: 5px 10px; border-radius: 3px;'>%s</code></p>\n" +
                        "            </div>\n" +
                        "            \n" +
                        "            <div class='warning'>\n" +
                        "                <strong>⚠️ Security Notice:</strong><br>\n" +
                        "                For your security, please change your password immediately after your first login.\n" +
                        "            </div>\n" +
                        "            \n" +
                        "            <h3>Getting Started:</h3>\n" +
                        "            <ol>\n" +
                        "                <li>Download the AgriBind mobile app or visit our web portal</li>\n" +
                        "                <li>Login using the credentials above</li>\n" +
                        "                <li>Complete your profile setup</li>\n" +
                        "                <li>Start managing your agricultural operations</li>\n" +
                        "            </ol>\n" +
                        "            \n" +
                        "            <center>\n" +
                        "                <a href='https://agribind.cm/login' class='button'>Login Now</a>\n" +
                        "            </center>\n" +
                        "            \n" +
                        "            <h3>Need Help?</h3>\n" +
                        "            <p>\n" +
                        "                📧 Email: support@agribind.cm<br>\n" +
                        "                📱 Phone: +237 XXX XXX XXX<br>\n" +
                        "                🌐 Website: https://agribind.cm\n" +
                        "            </p>\n" +
                        "        </div>\n" +
                        "        <div class='footer'>\n" +
                        "            <p>© 2025 AgriBind - Connecting Cameroon's Agricultural Ecosystem</p>\n" +
                        "            <p>This is an automated message. Please do not reply to this email.</p>\n" +
                        "        </div>\n" +
                        "    </div>\n" +
                        "</body>\n" +
                        "</html>",
                name, role, username, password
        );
    }

    private String formatUserType(String userType) {
        return switch (userType.toUpperCase()) {
            case "COOPERATIVE" -> "Cooperative Manager";
            case "GOVERNMENT" -> "Government Official";
            case "FARMER" -> "Farmer";
            default -> "User";
        };
    }

    /**
     * ✅ REST API fallback for welcome notification
     */
    private void sendWelcomeViaRest(WelcomeNotificationRequest request) {
        try {
            String url = notificationServiceUrl + "/api/notifications/welcome";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<WelcomeNotificationRequest> entity = new HttpEntity<>(request, headers);

            NotificationResponse response = restTemplate.postForObject(url, entity, NotificationResponse.class);
            log.info("✅ Welcome notification sent via REST for: {}", request.getUserId());
        } catch (Exception e) {
            log.error("❌ Failed to send welcome notification via REST", e);
        }
    }

    /**
     * ✅ REST API fallback for SMS
     */
    private void sendSmsViaRest(SmsNotificationRequest request) {
        try {
            String url = notificationServiceUrl + "/api/notifications/sms/send";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SmsNotificationRequest> entity = new HttpEntity<>(request, headers);

            NotificationResponse response = restTemplate.postForObject(url, entity, NotificationResponse.class);
            log.info("✅ SMS sent via REST to: {}", request.getPhoneNumber());
        } catch (Exception e) {
            log.error("❌ Failed to send SMS via REST to: {}", request.getPhoneNumber(), e);
        }
    }

    /**
     * ✅ REST API fallback for Email
     */
    private void sendEmailViaRest(EmailNotificationRequest request) {
        try {
            String url = notificationServiceUrl + "/api/notifications/email/send";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<EmailNotificationRequest> entity = new HttpEntity<>(request, headers);

            NotificationResponse response = restTemplate.postForObject(url, entity, NotificationResponse.class);
            log.info("✅ Email sent via REST to: {}", request.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send email via REST to: {}", request.getEmail(), e);
        }
    }

    // ===== DTOs =====

    @Data
    public static class SmsNotificationRequest {
        private String userId;
        private String phoneNumber;
        private String message;
        private String type;
        private String priority = "HIGH";
        private LocalDateTime timestamp = LocalDateTime.now();
    }

    @Data
    public static class EmailNotificationRequest {
        private String userId;
        private String email;
        private String subject;
        private String body;
        private String type;
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