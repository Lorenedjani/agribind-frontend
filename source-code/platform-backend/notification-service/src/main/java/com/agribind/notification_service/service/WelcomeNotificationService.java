package com.agribind.notification_service.service;

import cm.agribind.usermanagement.integration.dto.WelcomeNotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WelcomeNotificationService {

    private final EmailService emailService;
    private final SmsService smsService;

    public void sendWelcomeNotification(WelcomeNotificationRequest request) {
        log.info("Processing welcome notification for user: {}", request.getUserId());

        // Determine the actual username (prefer email, fallback to phone number)
        String actualUsername = determineUsername(request);

        // Send SMS if enabled
        if (request.isSendSms() && request.getPhoneNumber() != null) {
            try {
                String smsMessage = buildWelcomeSmsMessage(
                        request.getName(),
                        actualUsername, // Use actual username, not phone number
                        request.getTemporaryPassword(),
                        request.getUserType()
                );
                smsService.sendSms(request.getPhoneNumber(), smsMessage);
                log.info("Welcome SMS sent to: {}", request.getPhoneNumber());
            } catch (Exception e) {
                log.error("Failed to send welcome SMS to: {}", request.getPhoneNumber(), e);
            }
        }

        // Send Email if enabled
        if (request.isSendEmail() && request.getEmail() != null) {
            try {
                String emailBody = buildWelcomeEmailBody(
                        request.getName(),
                        actualUsername, // Use actual username
                        request.getTemporaryPassword(),
                        request.getUserType()
                );
                emailService.sendEmail(
                        request.getEmail(),
                        "Welcome to AgriBind - Your Login Credentials",
                        emailBody
                );
                log.info("Welcome email sent to: {}", request.getEmail());
            } catch (Exception e) {
                log.error("Failed to send welcome email to: {}", request.getEmail(), e);
            }
        }
    }

    private String determineUsername(WelcomeNotificationRequest request) {
        // Priority: 1. Dedicated username, 2. Email, 3. Phone number
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            return request.getUsername();
        } else if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            return request.getEmail();
        } else {
            return request.getPhoneNumber(); // Fallback to phone number
        }
    }

    private String buildWelcomeSmsMessage(String name, String username, String password, String userType) {
        String role = formatUserType(userType);
        return String.format(
                "Welcome to AgriBind, %s!\n\nYour %s account has been created.\n\nLogin Details:\nUsername: %s\nPassword: %s\n\n⚠️ Please change your password on first login.",
                name, role, username, password
        );
    }

    private String buildWelcomeEmailBody(String name, String username, String password, String userType) {
        String role = formatUserType(userType);
        return String.format(
                "<html><body>" +
                        "<h2>Welcome to AgriBind, %s!</h2>" +
                        "<p>Your <strong>%s account</strong> has been successfully created.</p>" +
                        "<p><strong>Username:</strong> %s</p>" +
                        "<p><strong>Temporary Password:</strong> %s</p>" +
                        "<p>Please change your password after first login.</p>" +
                        "</body></html>",
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
}