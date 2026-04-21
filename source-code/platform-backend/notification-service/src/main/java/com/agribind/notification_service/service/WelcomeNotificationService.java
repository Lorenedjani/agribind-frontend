package com.agribind.notification_service.service;

import cm.agribind.usermanagement.integration.dto.WelcomeNotificationRequest;
import com.agribind.notification_service.model.entity.NotificationHistory;
import com.agribind.notification_service.repository.NotificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WelcomeNotificationService {

    private final EmailService emailService;
    private final SmsService smsService;
    private final NotificationHistoryRepository notificationHistoryRepository;

    public void sendWelcomeNotification(WelcomeNotificationRequest request) {
        log.info("Processing welcome notification for user: {}", request.getUserId());

        // Determine the actual username (prefer email, fallback to phone number)
        String actualUsername = determineUsername(request);

        // ✅ Save Notification History to MySQL so it's readable on Mobile App
        try {
            NotificationHistory history = NotificationHistory.builder()
                    .userId(request.getUserId())
                    .category("GENERAL")
                    .title("Bienvenue sur AgriBind!")
                    .body(String.format("Votre compte a été créé. Vos identifiants de connexion:\nCode Utilisateur: %s\nMot de passe temporaire: %s", actualUsername, request.getTemporaryPassword()))
                    .createdAt(LocalDateTime.now())
                    .isRead(false)
                    .build();
            notificationHistoryRepository.save(history);
            log.info("Saved welcome notification to history DB for user: {}", request.getUserId());
        } catch (Exception e) {
            log.error("Failed to save welcome notification history to DB", e);
        }

        // Send SMS if enabled
        if (request.isSendSms() && request.getPhoneNumber() != null) {
            try {
                String smsMessage = buildWelcomeSmsMessage(request, actualUsername);
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

    private String buildWelcomeSmsMessage(WelcomeNotificationRequest request, String username) {
        String name = request.getName() != null ? request.getName() : "Utilisateur";
        String password = request.getTemporaryPassword() != null ? request.getTemporaryPassword() : "";
        String userType = request.getUserType() != null ? request.getUserType() : "USER";
        String phone = request.getPhoneNumber() != null ? request.getPhoneNumber() : "";
        boolean fr = request.getPreferredLanguage() == null
                || request.getPreferredLanguage().toLowerCase().startsWith("fr");

        if ("FARMER".equalsIgnoreCase(userType) && fr) {
            String reg = request.getRegistrationNumber() != null ? request.getRegistrationNumber() : "—";
            return String.format(
                    "Bienvenue sur AgriBind, %s !\n"
                            + "Votre compte agriculteur est créé.\n"
                            + "Connexion appli: tél. %s (OTP) ou code QR coop.\n"
                            + "Identifiant: %s\n"
                            + "Mot de passe provisoire: %s\n"
                            + "À changer à la 1ère connexion.\n"
                            + "N° enreg. / QR: %s",
                    name, phone, username, password, reg
            );
        }

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