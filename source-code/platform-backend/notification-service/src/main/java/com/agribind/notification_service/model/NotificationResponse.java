package com.agribind.notification_service.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private String notificationId;
    private String status; // PENDING, SENT, FAILED
    private String message;
    private LocalDateTime timestamp;
}