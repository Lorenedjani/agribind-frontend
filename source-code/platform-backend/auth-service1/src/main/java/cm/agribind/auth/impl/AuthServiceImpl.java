// Complete AuthServiceImpl.java with all methods implemented
package cm.agribind.auth.impl;

import cm.agribind.auth.dto.*;
import cm.agribind.auth.entity.*;
import cm.agribind.auth.exception.*;
import cm.agribind.auth.integration.*;
import cm.agribind.auth.repository.*;
import cm.agribind.auth.security.JwtService;
import cm.agribind.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserManagementClient userManagementClient;
    private final NotificationEventPublisher notificationPublisher;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${password-reset.token-expiration-minutes}")
    private Integer passwordResetExpirationMinutes;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        // Fetch user from user management service
        UserDto user;
        try {
            // Try to find by username (could be email or phone)
            user = userManagementClient.getUserByUsername(request.getUsername());
        } catch (Exception e) {
            log.error("User not found: {}", request.getUsername(), e);
            throw new UserNotFoundException("Invalid username or password");
        }

        // Validate account status
        validateAccountStatus(user);

        // Verify password
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            log.error("User has no password hash: {}", user.getUserId());
            throw new BadCredentialsException("Account not properly configured");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid password for user: {}", request.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }

        // Generate tokens
        return generateTokensAndResponse(user, request.getDeviceId());
    }

    @Override
    @Transactional
    public LoginResponse qrLogin(QrLoginRequest request) {
        log.info("QR login attempt for registration: {}", request.getRegistrationNumber());

        // Fetch user by registration number
        UserDto user;
        try {
            user = userManagementClient.getUserByRegistrationNumber(
                    request.getRegistrationNumber()
            );
        } catch (Exception e) {
            log.error("User not found for registration: {}", request.getRegistrationNumber());
            throw new UserNotFoundException("Invalid registration number");
        }

        // Validate account status
        validateAccountStatus(user);

        // Generate tokens (no password verification needed for QR login)
        return generateTokensAndResponse(user, request.getDeviceId());
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refresh token attempt");

        // Validate refresh token
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (refreshToken.getRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            throw new TokenExpiredException("Refresh token has expired");
        }

        // Get user details
        UserDto user = userManagementClient.getUserById(refreshToken.getUserId());
        validateAccountStatus(user);

        // Generate new access token
        Map<String, Object> claims = buildTokenClaims(user);
        String newAccessToken = jwtService.generateAccessToken(
                user.getUserId(),
                user.getRole(),
                claims
        );

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .userInfo(buildUserInfo(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(String userId, String deviceId) {
        log.info("Logout for user: {}, device: {}", userId, deviceId);

        if (deviceId != null) {
            refreshTokenRepository.revokeByUserIdAndDeviceId(userId, deviceId);
        } else {
            refreshTokenRepository.revokeAllByUserId(userId);
        }
    }

    @Override
    @Transactional
    public void changePassword(String userId, PasswordChangeRequest request) {
        log.info("Password change request for user: {}", userId);

        // Validate password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        // Get user details
        UserDto user = userManagementClient.getUserById(userId);

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        // Update password
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        userManagementClient.updatePassword(userId,
                PasswordUpdateRequest.builder()
                        .newPasswordHash(newPasswordHash)
                        .build()
        );

        // Revoke all existing tokens for security
        refreshTokenRepository.revokeAllByUserId(userId);

        log.info("Password changed successfully for user: {}", userId);
    }

    @Override
    @Transactional
    public void setFirstLoginPassword(String userId, FirstLoginPasswordRequest request) {
        log.info("First login password setup for user: {}", userId);

        // Validate password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        // Get user details
        UserDto user = userManagementClient.getUserById(userId);

        // Verify this is first login
        if (!user.getFirstLogin()) {
            throw new BusinessException("First login already completed");
        }

        // Update password
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        userManagementClient.updatePassword(userId,
                PasswordUpdateRequest.builder()
                        .newPasswordHash(newPasswordHash)
                        .build()
        );

        // Mark first login complete
        userManagementClient.markFirstLoginComplete(userId);

        // Update language if provided
        if (request.getPreferredLanguage() != null) {
            userManagementClient.updateLanguage(userId,
                    LanguageUpdateRequest.builder()
                            .language(request.getPreferredLanguage())
                            .build()
            );
        }

        log.info("First login setup completed for user: {}", userId);
    }

    @Override
    @Transactional
    public void initiatePasswordReset(PasswordResetInitRequest request) {
        log.info("Password reset initiated for email: {}", request.getEmail());

        // Get user by email
        UserDto user;
        try {
            user = userManagementClient.getUserByUsername(request.getEmail());
        } catch (Exception e) {
            // Don't reveal if email exists - security best practice
            log.warn("Password reset requested for non-existent email: {}", request.getEmail());
            return;
        }

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusMinutes(passwordResetExpirationMinutes);

        PasswordResetToken token = PasswordResetToken.builder()
                .token(resetToken)
                .userId(user.getUserId())
                .email(user.getEmail())
                .expiresAt(expiresAt)
                .used(false)
                .build();

        passwordResetRepository.save(token);

        // Send reset notification via SMS or push
        sendPasswordResetNotification(user, resetToken);

        log.info("Password reset token created for user: {}", user.getUserId());
    }

    @Override
    @Transactional
    public void completePasswordReset(PasswordResetCompleteRequest request) {
        log.info("Completing password reset with token");

        // Validate password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        // Find and validate token
        PasswordResetToken resetToken = passwordResetRepository
                .findByTokenAndUsedFalse(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        if (resetToken.isExpired()) {
            throw new TokenExpiredException("Reset token has expired");
        }

        // Update password
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        userManagementClient.updatePassword(resetToken.getUserId(),
                PasswordUpdateRequest.builder()
                        .newPasswordHash(newPasswordHash)
                        .build()
        );

        // Mark token as used
        passwordResetRepository.markAsUsed(request.getToken(), LocalDateTime.now());

        // Revoke all existing tokens for security
        refreshTokenRepository.revokeAllByUserId(resetToken.getUserId());

        log.info("Password reset completed for user: {}", resetToken.getUserId());
    }

    @Override
    @Transactional
    public void revokeAllTokens(String userId) {
        log.info("Revoking all tokens for user: {}", userId);
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    // ============= PRIVATE HELPER METHODS =============

    private void validateAccountStatus(UserDto user) {
        if (!user.getAccountEnabled()) {
            throw new BusinessException("Account is disabled");
        }

        if (user.getAccountLocked()) {
            throw new AccountLockedException("Account is locked due to multiple failed login attempts");
        }
    }

    private LoginResponse generateTokensAndResponse(UserDto user, String deviceId) {
        // Build claims
        Map<String, Object> claims = buildTokenClaims(user);

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(
                user.getUserId(),
                user.getRole(),
                claims
        );
        String refreshTokenValue = jwtService.generateRefreshToken(
                user.getUserId(),
                deviceId
        );

        // Save refresh token
        saveRefreshToken(user.getUserId(), refreshTokenValue, deviceId);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
                .userInfo(buildUserInfo(user))
                .build();
    }

    private Map<String, Object> buildTokenClaims(UserDto user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("cooperativeId", user.getCooperativeId());
        claims.put("language", user.getPreferredLanguage());
        return claims;
    }

    private LoginResponse.UserInfo buildUserInfo(UserDto user) {
        return LoginResponse.UserInfo.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .cooperativeId(user.getCooperativeId())
                .preferredLanguage(user.getPreferredLanguage())
                .firstLogin(user.getFirstLogin())
                .build();
    }

    private void saveRefreshToken(String userId, String token, String deviceId) {
        // Revoke existing tokens for this device
        if (deviceId != null) {
            refreshTokenRepository.revokeByUserIdAndDeviceId(userId, deviceId);
        }

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userId(userId)
                .deviceId(deviceId)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiration() / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    private void sendPasswordResetNotification(UserDto user, String resetToken) {
        try {
            // Prepare localized message based on user's language
            String message = getLocalizedResetMessage(user.getPreferredLanguage(), resetToken);

            // Send SMS if phone number exists
            if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
                notificationPublisher.sendSms(SmsRequest.builder()
                        .phoneNumber(user.getPhoneNumber())
                        .message(message)
                        .priority("HIGH")
                        .type("PASSWORD_RESET")
                        .build()
                );
            }

            // Send push notification for mobile users (farmers)
            if ("FARMER".equals(user.getRole())) {
                Map<String, String> data = new HashMap<>();
                data.put("resetToken", resetToken);
                data.put("expiresIn", String.valueOf(passwordResetExpirationMinutes));

                notificationPublisher.sendPushNotification(
                        PushNotificationRequest.builder()
                                .userId(user.getUserId())
                                .title(getLocalizedTitle(user.getPreferredLanguage()))
                                .body(message)
                                .type("PASSWORD_RESET")
                                .data(data)
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("Failed to send password reset notification: {}", e.getMessage());
            // Don't throw exception - password reset token is still valid
        }
    }

    private String getLocalizedResetMessage(String language, String token) {
        // Use first 8 characters of token for user-friendly code
        String shortCode = token.substring(0, 8).toUpperCase();

        return switch (language != null ? language.toUpperCase() : "EN") {
            case "FR" -> String.format(
                    "Votre code de réinitialisation: %s. Valide pendant %d minutes.",
                    shortCode,
                    passwordResetExpirationMinutes
            );
            case "FUL" -> String.format(
                    "Code resetaa ma: %s. Valid haa %d minutes.",
                    shortCode,
                    passwordResetExpirationMinutes
            );
            case "EWE" -> String.format(
                    "Code reset wo: %s. Valid na minit %d.",
                    shortCode,
                    passwordResetExpirationMinutes
            );
            case "DUA" -> String.format(
                    "Code reset ndé: %s. Valid na minute %d.",
                    shortCode,
                    passwordResetExpirationMinutes
            );
            default -> String.format(
                    "Your password reset code: %s. Valid for %d minutes.",
                    shortCode,
                    passwordResetExpirationMinutes
            );
        };
    }

    private String getLocalizedTitle(String language) {
        return switch (language != null ? language.toUpperCase() : "EN") {
            case "FR" -> "Réinitialisation du mot de passe";
            case "FUL" -> "Reset finnde code";
            case "EWE" -> "Reset password";
            case "DUA" -> "Reset password";
            default -> "Password Reset";
        };
    }
    // Implementation in AuthServiceImpl
    @Override
    @Transactional(readOnly = true)
    public List<DeviceInfo> getUserDevices(String userId) {
        log.info("Fetching active devices for user: {}", userId);

        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(userId);

        return tokens.stream()
                .filter(token -> !token.getRevoked() && !token.isExpired())
                .map(token -> DeviceInfo.builder()
                        .deviceId(token.getDeviceId())
                        .lastActive(token.getCreatedAt())
                        .isActive(true)
                        .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void revokeDevice(String userId, String deviceId) {
        log.info("Revoking device {} for user: {}", deviceId, userId);
        refreshTokenRepository.revokeByUserIdAndDeviceId(userId, deviceId);
    }


}