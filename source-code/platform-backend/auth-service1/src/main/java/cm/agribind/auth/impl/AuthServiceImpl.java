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

    @Value("${password-reset.token-expiration-minutes:30}")
    private Integer passwordResetExpirationMinutes;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("🔐 LOGIN ATTEMPT: User: {}", request.getUsername());

        // Step 1: Fetch user from User Management Service
        UserDto user;
        try {
            user = userManagementClient.getUserByUsername(request.getUsername());
            log.info("✅ User found: {}", user.getUserId());
        } catch (Exception e) {
            log.error("❌ User not found: {}", request.getUsername(), e);
            throw new UserNotFoundException("Invalid username or password");
        }

        // Step 2: Critical validation - Check passwordHash exists
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            log.error("❌ CRITICAL: User {} has no password hash!", user.getUserId());
            throw new BadCredentialsException("Account not properly configured. Contact support.");
        }

        // Step 3: Validate account status
        validateAccountStatus(user);

        // Step 4: Verify password
        log.debug("🔍 Verifying password for user: {}", user.getUserId());
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());

        if (!passwordMatches) {
            log.warn("❌ Invalid password for user: {}", request.getUsername());
            // In production, implement failed login tracking here
            throw new BadCredentialsException("Invalid username or password");
        }

        log.info("✅ Password verified successfully for user: {}", user.getUserId());

        // Step 5: Generate tokens and response
        LoginResponse response = generateTokensAndResponse(user, request.getDeviceId());

        log.info("✅ LOGIN SUCCESSFUL: User: {}, FirstLogin: {}",
                user.getUserId(), response.getUserInfo().getFirstLogin());

        return response;
    }

    @Override
    @Transactional
    public LoginResponse qrLogin(QrLoginRequest request) {
        log.info("📱 QR LOGIN ATTEMPT: Registration: {}", request.getRegistrationNumber());

        // Fetch user by registration number
        UserDto user;
        try {
            user = userManagementClient.getUserByRegistrationNumber(request.getRegistrationNumber());
        } catch (Exception e) {
            log.error("❌ User not found for registration: {}", request.getRegistrationNumber());
            throw new UserNotFoundException("Invalid registration number");
        }

        // Validate account status
        validateAccountStatus(user);

        // Generate tokens (no password verification needed for QR login)
        LoginResponse response = generateTokensAndResponse(user, request.getDeviceId());

        log.info("✅ QR LOGIN SUCCESSFUL: User: {}", user.getUserId());
        return response;
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        log.info("🔄 REFRESH TOKEN ATTEMPT");

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

        log.info("✅ Token refreshed successfully for user: {}", user.getUserId());

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
        log.info("🚪 LOGOUT: User: {}, Device: {}", userId, deviceId);

        if (deviceId != null) {
            refreshTokenRepository.revokeByUserIdAndDeviceId(userId, deviceId);
        } else {
            refreshTokenRepository.revokeAllByUserId(userId);
        }

        log.info("✅ Logout successful for user: {}", userId);
    }

    @Override
    @Transactional
    public void changePassword(String userId, PasswordChangeRequest request) {
        log.info("🔑 PASSWORD CHANGE: User: {}", userId);

        // Validate passwords match
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

        log.info("✅ Password changed successfully for user: {}", userId);
    }

    @Override
    @Transactional
    public void setFirstLoginPassword(String userId, FirstLoginPasswordRequest request) {
        log.info("🆕 FIRST LOGIN PASSWORD SETUP: User: {}", userId);

        // Validate passwords match
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

        log.info("✅ First login password setup completed for user: {}", userId);
    }

    @Override
    @Transactional
    public void initiatePasswordReset(PasswordResetInitRequest request) {
        log.info("🔐 PASSWORD RESET INITIATED: Email: {}", request.getEmail());

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

        // Send reset notification
        sendPasswordResetNotification(user, resetToken);

        log.info("✅ Password reset token created for user: {}", user.getUserId());
    }

    @Override
    @Transactional
    public void completePasswordReset(PasswordResetCompleteRequest request) {
        log.info("🔐 COMPLETING PASSWORD RESET");

        // Validate passwords match
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

        log.info("✅ Password reset completed for user: {}", resetToken.getUserId());
    }

    @Override
    @Transactional
    public void revokeAllTokens(String userId) {
        log.info("🔒 Revoking all tokens for user: {}", userId);
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceInfo> getUserDevices(String userId) {
        log.info("📱 Fetching devices for user: {}", userId);

        List<RefreshToken> tokens = refreshTokenRepository.findByUserId(userId);

        return tokens.stream()
                .filter(token -> !token.getRevoked() && !token.isExpired())
                .map(token -> DeviceInfo.builder()
                        .deviceId(token.getDeviceId())
                        .lastActive(token.getCreatedAt())
                        .isActive(true)
                        .build()
                )
                .toList();
    }

    @Override
    @Transactional
    public void revokeDevice(String userId, String deviceId) {
        log.info("🔒 Revoking device {} for user: {}", deviceId, userId);
        refreshTokenRepository.revokeByUserIdAndDeviceId(userId, deviceId);
    }

    // ============= PRIVATE HELPER METHODS =============

    private void validateAccountStatus(UserDto user) {
        log.debug("🔍 Validating account - Status: {}, Locked: {}, Enabled: {}",
                user.getStatus(), user.getAccountLocked(), user.getAccountEnabled());

        // Check if account is locked
        if (user.getAccountLocked()) {
            log.warn("❌ Account locked: {}", user.getUserId());
            throw new AccountLockedException("Account is locked. Contact support.");
        }

        // Check if account is enabled
        if (!user.getAccountEnabled()) {
            log.warn("❌ Account disabled: {}", user.getUserId());
            throw new BusinessException("Account is disabled. Contact support.");
        }

        // Check account status string
        if (user.getStatus() != null && !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            log.warn("❌ Account status is not ACTIVE: {}", user.getStatus());
            throw new BusinessException("Account status is: " + user.getStatus());
        }

        log.debug("✅ Account validation passed");
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
        claims.put("userId", user.getUserId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole());
        claims.put("cooperativeId", user.getCooperativeId());
        claims.put("language", user.getPreferredLanguage());
        return claims;
    }

    private LoginResponse.UserInfo buildUserInfo(UserDto user) {
        return LoginResponse.UserInfo.builder()
                .userId(user.getUserId())
                .username(user.getEmail() != null ? user.getEmail() : user.getPhoneNumber())
                .email(user.getEmail())
                .role(user.getRole())
                .cooperativeId(user.getCooperativeId())
                .preferredLanguage(user.getPreferredLanguage())
                .firstLogin(user.getFirstLogin())
                .build();
    }

    // Update this method in AuthServiceImpl.java around line 425

    private void saveRefreshToken(String userId, String token, String deviceId) {
        log.debug("Saving refresh token for user: {}, device: {}", userId, deviceId);

        try {
            // IMPORTANT: Revoke existing tokens for this user and device FIRST
            if (deviceId != null && !deviceId.isEmpty()) {
                // Delete existing tokens for this device to prevent duplicates
                refreshTokenRepository.deleteByUserIdAndDeviceId(userId, deviceId);
                log.debug("Deleted existing tokens for user: {} and device: {}", userId, deviceId);
            } else {
                // If no device ID, revoke all tokens for this user
                refreshTokenRepository.deleteByUserId(userId);
                log.debug("Deleted all existing tokens for user: {}", userId);
            }

            // Flush the deletions to ensure they're committed before insert
            refreshTokenRepository.flush();

            // Now create and save the new token
            RefreshToken refreshToken = RefreshToken.builder()
                    .token(token)
                    .userId(userId)
                    .deviceId(deviceId != null ? deviceId : "default")
                    .expiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiration() / 1000))
                    .revoked(false)
                    .build();

            refreshTokenRepository.save(refreshToken);
            log.info("✅ Refresh token saved successfully for user: {}", userId);

        } catch (Exception e) {
            log.error("❌ Failed to save refresh token for user: {}", userId, e);
            // Don't throw exception - allow login to continue even if token save fails
            // The user can still use their access token
        }
    }

    private void sendPasswordResetNotification(UserDto user, String resetToken) {
        try {
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

            log.info("✅ Password reset notification sent");
        } catch (Exception e) {
            log.error("❌ Failed to send password reset notification", e);
        }
    }

    private String getLocalizedResetMessage(String language, String token) {
        String shortCode = token.substring(0, 8).toUpperCase();

        return switch (language != null ? language.toUpperCase() : "EN") {
            case "FR" -> String.format(
                    "Votre code de réinitialisation: %s. Valide pendant %d minutes.",
                    shortCode, passwordResetExpirationMinutes
            );
            default -> String.format(
                    "Your password reset code: %s. Valid for %d minutes.",
                    shortCode, passwordResetExpirationMinutes
            );
        };
    }
}