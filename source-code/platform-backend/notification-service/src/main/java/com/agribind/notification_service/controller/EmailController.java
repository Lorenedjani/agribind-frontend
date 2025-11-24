package com.agribind.notification_service.controller;

import com.agribind.notification_service.service.EmailService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notifications/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<EmailResponse> sendEmail(@RequestBody EmailRequest request) {
        log.info("Received email request for: {}", request.getEmail());

        try {
            emailService.sendEmail(
                    request.getEmail(),
                    request.getSubject(),
                    request.getBody()
            );

            return ResponseEntity.ok(new EmailResponse(
                    true,
                    "Email sent successfully",
                    request.getEmail()
            ));
        } catch (Exception e) {
            log.error("Email sending failed", e);
            return ResponseEntity.ok(new EmailResponse(
                    false,
                    "Email sending failed: " + e.getMessage(),
                    request.getEmail()
            ));
        }
    }

    @Data
    public static class EmailRequest {
        private String email;
        private String subject;
        private String body;
        private String type;
        private String priority;
    }

    @Data
    public static class EmailResponse {
        private final boolean success;
        private final String message;
        private final String email;
    }
}