package com.agribind.notification_service.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationRequest {
    private String userId;
    private String type; // EMAIL, SMS, PUSH
    private String subject;
    private String message;
    private String recipient;
    private LocalDateTime timestamp;
}
