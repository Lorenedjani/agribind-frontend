package cm.agribind.usermanagement.service;

import cm.agribind.usermanagement.dto.command.GenerateQRCodeCommand;
import cm.agribind.usermanagement.dto.response.QRCodeResponse;
import cm.agribind.usermanagement.entity.User;
import cm.agribind.usermanagement.exception.UserNotFoundException;
import cm.agribind.usermanagement.repository.UserRepository;
import cm.agribind.usermanagement.service.QRCodeService;
import cm.agribind.usermanagement.util.QRCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class QRCodeServiceImpl implements QRCodeService {

    private final UserRepository userRepository;
    private final QRCodeGenerator qrCodeGenerator;

    @Override
    public QRCodeResponse generateQRCode(GenerateQRCodeCommand command) {
        log.info("Generating QR code for user: {}, purpose: {}", command.getUserId(), command.getPurpose());

        User user = userRepository.findByUserId(command.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + command.getUserId()));

        String qrData = generateQRData(user, command.getPurpose());
        byte[] qrImage = qrCodeGenerator.generateQRCodeImage(qrData, command.getSize(), command.getFormat());

        String base64Image = Base64.getEncoder().encodeToString(qrImage);

        QRCodeResponse response = new QRCodeResponse(
                user.getUserId(),
                command.getPurpose(),
                "data:image/png;base64," + base64Image,
                qrData
        );
        response.setSize(command.getSize());
        response.setFormat(command.getFormat());

        return response;
    }

    @Override
    public QRCodeResponse generateRegistrationQRCode(String userId) {
        GenerateQRCodeCommand command = new GenerateQRCodeCommand();
        command.setUserId(userId);
        command.setPurpose("REGISTRATION");
        command.setSize(300);
        command.setFormat("PNG");

        return generateQRCode(command);
    }

    @Override
    public QRCodeResponse generateLoginQRCode(String userId) {
        GenerateQRCodeCommand command = new GenerateQRCodeCommand();
        command.setUserId(userId);
        command.setPurpose("LOGIN");
        command.setSize(250);
        command.setFormat("PNG");

        return generateQRCode(command);
    }

    @Override
    public User validateQRCode(String qrData) {
        log.debug("Validating QR code data: {}", qrData);

        try {
            // Parse QR data (format: "TYPE:USER_ID:PURPOSE:TIMESTAMP:SIGNATURE")
            String[] parts = qrData.split(":");
            if (parts.length != 5) {
                throw new IllegalArgumentException("Invalid QR code format");
            }

            String userId = parts[1];
            String purpose = parts[2];

            User user = userRepository.findByUserId(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

            // Validate purpose and expiration
            if (!isValidPurpose(purpose) || isQRCodeExpired(parts[3])) {
                throw new IllegalArgumentException("Invalid or expired QR code");
            }

            log.info("QR code validated successfully for user: {}", userId);
            return user;

        } catch (Exception e) {
            log.error("QR code validation failed", e);
            throw new IllegalArgumentException("Invalid QR code: " + e.getMessage());
        }
    }

    @Override
    public String generateQRData(User user, String purpose) {
        long timestamp = System.currentTimeMillis();
        String data = String.format("USER:%s:%s:%d", user.getUserId(), purpose, timestamp);

        // In real implementation, add digital signature
        String signature = generateSignature(data);

        return data + ":" + signature;
    }

    @Override
    public String generateRegistrationData(User user) {
        return generateQRData(user, "REGISTRATION");
    }

    private boolean isValidPurpose(String purpose) {
        return "REGISTRATION".equals(purpose) || "LOGIN".equals(purpose) || "PROFILE".equals(purpose);
    }

    private boolean isQRCodeExpired(String timestampStr) {
        try {
            long timestamp = Long.parseLong(timestampStr);
            long currentTime = System.currentTimeMillis();
            // QR codes expire after 30 minutes
            return (currentTime - timestamp) > (30 * 60 * 1000);
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private String generateSignature(String data) {
        // In real implementation, use proper cryptographic signing
        // For now, return a simple hash
        return Integer.toHexString(data.hashCode());
    }
}