package com.agribind.notification_service.controller;

import cm.agribind.usermanagement.integration.dto.WelcomeNotificationRequest;
import com.agribind.notification_service.service.WelcomeNotificationService;
import com.agribind.notification_service.service.EmailService;
import com.agribind.notification_service.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Direct notification controller that bypasses message queues
 * Useful for development when RabbitMQ/Kafka are not available
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications/direct")
@RequiredArgsConstructor
public class DirectNotificationController {

    private final WelcomeNotificationService welcomeNotificationService;
    private final EmailService emailService;
    private final SmsService smsService;

    @PostMapping("/welcome")
    public ResponseEntity<DirectNotificationResponse> sendWelcomeDirect(
            @RequestBody WelcomeNotificationRequest request) {

        log.info("Received DIRECT welcome notification request for user: {}", request.getUserId());

        try {
            // Send synchronously without queue
            welcomeNotificationService.sendWelcomeNotification(request);

            return ResponseEntity.ok(new DirectNotificationResponse(
                    true,
                    "Welcome notification sent successfully (DIRECT mode)",
                    request.getUserId()
            ));
        } catch (Exception e) {
            log.error("Direct welcome notification failed", e);
            return ResponseEntity.ok(new DirectNotificationResponse(
                    false,
                    "Welcome notification failed: " + e.getMessage(),
                    request.getUserId()
            ));
        }
    }

    @PostMapping("/email")
    public ResponseEntity<DirectNotificationResponse> sendEmailDirect(@RequestBody DirectEmailRequest request) {
        log.info("Received DIRECT email request for: {}", request.email());  // ✅ Changed to email()

        try {
            emailService.sendEmail(request.email(), request.subject(), request.body());  // ✅ Changed

            return ResponseEntity.ok(new DirectNotificationResponse(
                    true,
                    "Email sent successfully (DIRECT mode)",
                    request.email()  // ✅ Changed
            ));
        } catch (Exception e) {
            log.error("Direct email failed", e);
            return ResponseEntity.ok(new DirectNotificationResponse(
                    false,
                    "Email failed: " + e.getMessage(),
                    request.email()  // ✅ Changed
            ));
        }
    }

    @PostMapping("/sms")
    public ResponseEntity<DirectNotificationResponse> sendSmsDirect(@RequestBody DirectSmsRequest request) {
        log.info("Received DIRECT SMS request for: {}", request.phoneNumber());  // ✅ Changed

        try {
            smsService.sendSms(request.phoneNumber(), request.message());  // ✅ Changed

            return ResponseEntity.ok(new DirectNotificationResponse(
                    true,
                    "SMS sent successfully (DIRECT mode)",
                    request.phoneNumber()  // ✅ Changed
            ));
        } catch (Exception e) {
            log.error("Direct SMS failed", e);
            return ResponseEntity.ok(new DirectNotificationResponse(
                    false,
                    "SMS failed: " + e.getMessage(),
                    request.phoneNumber()  // ✅ Changed
            ));
        }
    }

    public record DirectNotificationResponse(
            boolean success,
            String message,
            String recipient
    ) {}

    public record DirectEmailRequest(
            String email,
            String subject,
            String body
    ) {}

    public record DirectSmsRequest(
            String phoneNumber,
            String message
    ) {}
}