package cm.agribind.auth.service;

import cm.agribind.auth.dto.*;

import java.util.List;
import java.util.Map;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse qrLogin(QrLoginRequest request);
    Map<String, Object> sendPhoneOtp(PhoneOtpSendRequest request);
    LoginResponse verifyPhoneOtp(PhoneOtpVerifyRequest request);
    LoginResponse refreshToken(RefreshTokenRequest request);
    void logout(String userId, String deviceId);
    void changePassword(String userId, PasswordChangeRequest request);
    void setFirstLoginPassword(String userId, FirstLoginPasswordRequest request);
    void initiatePasswordReset(PasswordResetInitRequest request);
    void completePasswordReset(PasswordResetCompleteRequest request);
    void revokeAllTokens(String userId);
    List<DeviceInfo> getUserDevices(String userId);
    void revokeDevice(String userId, String deviceId);
}