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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

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
    @Value("${phone-otp.expiration-minutes:5}")
    private Integer phoneOtpExpirationMinutes;
    @Value("${phone-otp.max-attempts:5}")
    private Integer phoneOtpMaxAttempts;

    private final Map<String, OtpSession> otpSessions = new ConcurrentHashMap<>();

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
        log.info("📱 QR LOGIN ATTEMPT: Data: {}", request.getQrData());

        String qrData = request.getQrData();
        if (qrData == null || qrData.isEmpty()) {
            throw new BadCredentialsException("QR Data is required");
        }

        String[] parts = qrData.split(":");
        if (parts.length < 4) {
            log.error("❌ Invalid QR code format: {}", qrData);
            throw new BadCredentialsException("Invalid QR code format");
        }

        String prefix = parts[0];
        String userId = parts[1];
        String purpose = parts[2];

        if (!"USER".equals(prefix) || !"LOGIN".equals(purpose)) {
            log.error("❌ Not a login QR code or invalid prefix: {}", qrData);
            throw new BadCredentialsException("Invalid QR code purpose");
        }

        // Fetch user by user id from QR Code payload
        UserDto user;
        try {
            user = userManagementClient.getUserById(userId);
        } catch (Exception e) {
            log.error("❌ User not found for userId: {}", userId);
            throw new UserNotFoundException("User not found or invalid QR code");
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
    public Map<String, Object> sendPhoneOtp(PhoneOtpSendRequest request) {
        String normalizedPhone = normalizePhoneNumber(request.getPhoneNumber());
        UserDto user;
        try {
            user = userManagementClient.getUserByPhone(normalizedPhone);
        } catch (Exception e) {
            throw new UserNotFoundException("No user found for this phone number");
        }

        validateAccountStatus(user);
        String otpCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        OtpSession session = OtpSession.builder()
                .phoneNumber(normalizedPhone)
                .otpCode(otpCode)
                .attempts(0)
                .expiresAt(LocalDateTime.now().plusMinutes(phoneOtpExpirationMinutes))
                .build();
        otpSessions.put(normalizedPhone, session);

        // Placeholder for SMS provider integration. Keep visible during sprint delivery.
        log.info("OTP for {} is {} (dev visibility)", normalizedPhone, otpCode);

        String language = user.getPreferredLanguage();
        String messageText = (language != null && language.equalsIgnoreCase("FR"))
                ? "Votre code OTP pour AgriBind est : " + otpCode
                : "Your AgriBind login OTP is: " + otpCode;

        try {
            notificationPublisher.sendSms(SmsRequest.builder()
                    .phoneNumber(normalizedPhone)
                    .message(messageText)
                    .priority("HIGH")
                    .type("OTP_VERIFICATION")
                    .build());
            log.info("✅ OTP SMS dispatched to notification-service for {}", normalizedPhone);
        } catch (Exception e) {
            log.error("❌ Failed to send OTP SMS to {}", normalizedPhone, e);
        }

        return Map.of(
                "phoneNumber", normalizedPhone,
                "expiresInSeconds", phoneOtpExpirationMinutes * 60,
                "attemptsRemaining", phoneOtpMaxAttempts
        );
    }

    @Override
    @Transactional
    public LoginResponse verifyPhoneOtp(PhoneOtpVerifyRequest request) {
        String normalizedPhone = normalizePhoneNumber(request.getPhoneNumber());
        OtpSession session = otpSessions.get(normalizedPhone);

        if (session == null) {
            throw new InvalidTokenException("No OTP request found for this phone number");
        }
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpSessions.remove(normalizedPhone);
            throw new TokenExpiredException("OTP has expired");
        }
        if (session.getAttempts() >= phoneOtpMaxAttempts) {
            otpSessions.remove(normalizedPhone);
            throw new BusinessException("Maximum OTP attempts exceeded");
        }
        if (!session.getOtpCode().equals(request.getOtpCode())) {
            session.setAttempts(session.getAttempts() + 1);
            otpSessions.put(normalizedPhone, session);
            throw new BadCredentialsException("Invalid OTP code");
        }

        UserDto user = userManagementClient.getUserByPhone(normalizedPhone);
        validateAccountStatus(user);
        otpSessions.remove(normalizedPhone);
        return generateTokensAndResponse(user, request.getDeviceId());
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

        if (user.getAccountLocked()) {
            log.warn("❌ Account locked: {}", user.getUserId());
            throw new AccountLockedException("Account is locked. Contact support.");
        }

        if (!user.getAccountEnabled()) {
            log.warn("❌ Account disabled: {}", user.getUserId());
            throw new BusinessException("Account is disabled. Contact support.");
        }

        if (user.getStatus() != null && !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            log.warn("❌ Account status is not ACTIVE: {}", user.getStatus());
            throw new BusinessException("Account status is: " + user.getStatus());
        }

        log.debug("✅ Account validation passed");
    }

    private LoginResponse generateTokensAndResponse(UserDto user, String deviceId) {
        // Build claims
        Map<String, Object> claims = buildTokenClaims(user);

        // Generate access token
        String accessToken = jwtService.generateAccessToken(
                user.getUserId(),
                user.getRole(),
                claims
        );

        // Generate refresh token — JwtService.generateRefreshToken() MUST include
        // a jti (UUID) claim so that two users sharing the same deviceId can never
        // produce an identical token string. See JwtService fix notes below.
        String refreshTokenValue = jwtService.generateRefreshToken(
                user.getUserId(),
                deviceId
        );

        // Persist refresh token
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
        if (user.getRegion() != null && !user.getRegion().isBlank()) {
            claims.put("region", user.getRegion().trim());
        }
        return claims;
    }

    private LoginResponse.UserInfo buildUserInfo(UserDto user) {
        return LoginResponse.UserInfo.builder()
                .userId(user.getUserId())
                .username(user.getEmail() != null ? user.getEmail() : user.getPhoneNumber())
                .email(user.getEmail())
                .role(user.getRole())
                .cooperativeId(user.getCooperativeId())
                .region(user.getRegion())
                .preferredLanguage(user.getPreferredLanguage())
                .firstLogin(user.getFirstLogin())
                .build();
    }

    /**
     * Persists a new refresh token for the given user/device pair.
     *
     * Deletion order:
     *   1. Delete by (userId, deviceId)  — removes this user's old token on this device.
     *   2. Delete by token value         — safety net: removes any OTHER user's row that
     *      happens to carry the same token string (possible when two users share a
     *      deviceId and JwtService produces an identical JWT within the same second).
     *
     * The real long-term fix is to add a UUID jti claim in JwtService so that no two
     * generated tokens can ever be equal. Both defences are kept here.
     */
    private void saveRefreshToken(String userId, String token, String deviceId) {
        log.debug("Saving refresh token for user: {}, device: {}", userId, deviceId);

        // 1. Delete this user's previous token on the same device
        if (deviceId != null && !deviceId.isEmpty()) {
            refreshTokenRepository.deleteByUserIdAndDeviceId(userId, deviceId);
            log.debug("Deleted existing tokens for user: {} and device: {}", userId, deviceId);
        } else {
            refreshTokenRepository.deleteByUserId(userId);
            log.debug("Deleted all existing tokens for user: {}", userId);
        }

        // 2. Safety net: delete any stale row (from any user) with this exact token value
        refreshTokenRepository.deleteByToken(token);
        log.debug("Cleared any duplicate token value entries for token prefix: {}...",
                token.length() > 20 ? token.substring(0, 20) : token);

        // 3. Insert new token
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userId(userId)
                .deviceId(deviceId != null ? deviceId : "default")
                .expiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiration() / 1000))
                .revoked(false)
                .build();

        refreshTokenRepository.saveAndFlush(refreshToken);
        log.info("✅ Refresh token saved successfully for user: {}", userId);
    }

    private void sendPasswordResetNotification(UserDto user, String resetToken) {
        try {
            String message = getLocalizedResetMessage(user.getPreferredLanguage(), resetToken);

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

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber == null ? "" : phoneNumber.trim().replace(" ", "");
    }

    @lombok.Data
    @lombok.Builder
    private static class OtpSession {
        private String phoneNumber;
        private String otpCode;
        private int attempts;
        private LocalDateTime expiresAt;
    }
}