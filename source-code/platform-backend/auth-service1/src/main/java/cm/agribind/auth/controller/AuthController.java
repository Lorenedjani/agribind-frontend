package cm.agribind.auth.controller;

import cm.agribind.auth.dto.*;
import cm.agribind.auth.entity.RefreshToken;
import cm.agribind.auth.repository.RefreshTokenRepository;
import cm.agribind.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    // AuthController.java (add this field)
    private final RefreshTokenRepository refreshTokenRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for: {}", request.getUsername());

        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(
                AuthResponse.success("Login successful", response)
        );
    }

    @PostMapping("/qr-login")
    public ResponseEntity<AuthResponse> qrLogin(@Valid @RequestBody QrLoginRequest request) {
        log.info("QR login request received for: {}", request.getRegistrationNumber());

        LoginResponse response = authService.qrLogin(request);
        return ResponseEntity.ok(
                AuthResponse.success("QR login successful", response)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Refresh token request received");

        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(
                AuthResponse.success("Token refreshed successfully", response)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(
            @AuthenticationPrincipal String userId,
            @RequestParam(required = false) String deviceId) {
        log.info("Logout request for user: {}", userId);

        authService.logout(userId, deviceId);
        return ResponseEntity.ok(
                AuthResponse.success("Logged out successfully", null)
        );
    }

    @PutMapping("/password/change")
    public ResponseEntity<AuthResponse> changePassword(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody PasswordChangeRequest request) {
        log.info("Password change request for user: {}", userId);

        authService.changePassword(userId, request);
        return ResponseEntity.ok(
                AuthResponse.success("Password changed successfully", null)
        );
    }

    @PostMapping("/password/first-login")
    public ResponseEntity<AuthResponse> setFirstLoginPassword(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody FirstLoginPasswordRequest request) {
        log.info("First login password setup for user: {}", userId);

        authService.setFirstLoginPassword(userId, request);
        return ResponseEntity.ok(
                AuthResponse.success("Password setup completed", null)
        );
    }

    @PostMapping("/password/reset/init")
    public ResponseEntity<AuthResponse> initiatePasswordReset(
            @Valid @RequestBody PasswordResetInitRequest request) {
        log.info("Password reset initiation for email: {}", request.getEmail());

        authService.initiatePasswordReset(request);
        return ResponseEntity.ok(
                AuthResponse.success(
                        "If the email exists, a reset link has been sent",
                        null
                )
        );
    }

    @PostMapping("/password/reset/complete")
    public ResponseEntity<AuthResponse> completePasswordReset(
            @Valid @RequestBody PasswordResetCompleteRequest request) {
        log.info("Password reset completion request");

        authService.completePasswordReset(request);
        return ResponseEntity.ok(
                AuthResponse.success("Password reset successfully", null)
        );
    }

    @PostMapping("/revoke-all")
    public ResponseEntity<AuthResponse> revokeAllTokens(
            @AuthenticationPrincipal String userId) {
        log.info("Revoke all tokens for user: {}", userId);

        authService.revokeAllTokens(userId);
        return ResponseEntity.ok(
                AuthResponse.success("All tokens revoked successfully", null)
        );
    }

    @GetMapping("/health")
    public ResponseEntity<AuthResponse> health() {
        return ResponseEntity.ok(
                AuthResponse.success("Auth service is running", null)
        );
    }

    /**
     * Device Management Endpoints
     * For cooperatives to manage farmer devices
     */

    @GetMapping("/devices/{userId}")
    public ResponseEntity<AuthResponse> getUserDevices(@PathVariable String userId) {
        log.info("Fetching devices for user: {}", userId);

        List<RefreshToken> devices = refreshTokenRepository.findByUserId(userId);

        List<DeviceInfo> deviceList = devices.stream()
                .filter(token -> !token.getRevoked() && !token.isExpired())
                .map(token -> DeviceInfo.builder()
                        .deviceId(token.getDeviceId())
                        .lastActive(token.getCreatedAt())
                        .isActive(true)
                        .build()
                )
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                AuthResponse.success("Devices retrieved successfully", deviceList)
        );
    }

    @DeleteMapping("/devices/{userId}/{deviceId}")
    public ResponseEntity<AuthResponse> revokeDevice(
            @PathVariable String userId,
            @PathVariable String deviceId) {
        log.info("Revoking device {} for user: {}", deviceId, userId);

        refreshTokenRepository.revokeByUserIdAndDeviceId(userId, deviceId);

        return ResponseEntity.ok(
                AuthResponse.success("Device revoked successfully", null)
        );
    }

    @DeleteMapping("/devices/{userId}")
    public ResponseEntity<AuthResponse> revokeAllDevices(@PathVariable String userId) {
        log.info("Revoking all devices for user: {}", userId);

        refreshTokenRepository.revokeAllByUserId(userId);

        return ResponseEntity.ok(
                AuthResponse.success("All devices revoked successfully", null)
        );
    }
}