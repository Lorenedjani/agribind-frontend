package com.agribind.notification_service.controller;

import cm.agribind.usermanagement.integration.dto.WelcomeNotificationRequest;
import com.agribind.notification_service.service.WelcomeNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class WelcomeNotificationController {

    private final WelcomeNotificationService welcomeNotificationService;

    @PostMapping("/welcome")
    public ResponseEntity<WelcomeNotificationResponse> sendWelcomeNotification(
            @RequestBody WelcomeNotificationRequest request) {

        log.info("Received welcome notification request for user: {}", request.getUserId());

        try {
            welcomeNotificationService.sendWelcomeNotification(request);

            return ResponseEntity.ok(new WelcomeNotificationResponse(
                    true,
                    "SUCCESS",
                    "Welcome notification sent successfully",
                    request.getUserId(),
                    null
            ));
        } catch (Exception e) {
            log.error("Welcome notification failed for user: {}", request.getUserId(), e);
            return ResponseEntity.ok(new WelcomeNotificationResponse(
                    false,
                    "FAILED",
                    "Welcome notification failed: " + e.getMessage(),
                    request.getUserId(),
                    null
            ));
        }
    }

    /**
     * Response format that matches NotificationServiceClient expectations
     */
    public record WelcomeNotificationResponse(
            boolean success,
            String status,        // SUCCESS, FAILED, PENDING
            String message,
            String userId,
            String notificationId
    ) {}
}