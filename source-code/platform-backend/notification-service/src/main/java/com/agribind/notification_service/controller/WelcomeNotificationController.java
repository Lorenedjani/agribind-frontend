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
                    "Welcome notification sent successfully",
                    request.getUserId()
            ));
        } catch (Exception e) {
            log.error("Welcome notification failed", e);
            return ResponseEntity.ok(new WelcomeNotificationResponse(
                    false,
                    "Welcome notification failed: " + e.getMessage(),
                    request.getUserId()
            ));
        }
    }

    public record WelcomeNotificationResponse(
            boolean success,
            String message,
            String userId
    ) {}
}