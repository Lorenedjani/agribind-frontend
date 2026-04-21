package cm.agribind.usermanagement.util;

import cm.agribind.usermanagement.entity.User;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Slf4j
@Component
public class QRCodeGenerator {

    private static final String QR_CODE_CHARSET = "UTF-8";

    public byte[] generateQRCodeImage(String text, int width, int height, String format) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            ImageIO.write(bufferedImage, format, outputStream);

            return outputStream.toByteArray();

        } catch (WriterException | IOException e) {
            log.error("Error generating QR code", e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    public byte[] generateQRCodeImage(String text, int size, String format) {
        return generateQRCodeImage(text, size, size, format);
    }

    public String generateQRData(User user, String purpose) {
        long timestamp = System.currentTimeMillis();
        String data = String.format("AGRICULTURE:%s:%s:%s:%d",
                user.getType(),
                user.getUserId(),
                purpose,
                timestamp
        );

        // Add signature for security
        String signature = generateSignature(data, user.getRegistrationNumber());
        return data + ":" + signature;
    }

    public String generateRegistrationData(User user) {
        return generateQRData(user, "REGISTRATION");
    }

    public String generateLoginData(User user) {
        return generateQRData(user, "LOGIN");
    }

    public String generateProfileData(User user) {
        return generateQRData(user, "PROFILE");
    }

    private String generateSignature(String data, String secret) {
        try {
            // Simple signature generation - in production, use proper cryptographic signing
            String toSign = data + ":" + secret;
            return Base64.getEncoder().encodeToString(
                    Integer.toString(toSign.hashCode()).getBytes(QR_CODE_CHARSET)
            );
        } catch (Exception e) {
            log.error("Error generating signature", e);
            return "unsigned";
        }
    }

    public boolean validateSignature(String data, String signature, String secret) {
        try {
            String expectedSignature = generateSignature(data, secret);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error validating signature", e);
            return false;
        }
    }

    public String parseQRCodePurpose(String qrData) {
        try {
            String[] parts = qrData.split(":");
            if (parts.length >= 4) {
                return parts[3]; // Purpose is at index 3
            }
        } catch (Exception e) {
            log.error("Error parsing QR code data", e);
        }
        return "UNKNOWN";
    }

    public String parseQRCodeUserId(String qrData) {
        try {
            String[] parts = qrData.split(":");
            if (parts.length >= 3) {
                return parts[2]; // User ID is at index 2
            }
        } catch (Exception e) {
            log.error("Error parsing QR code data", e);
        }
        return null;
    }
}