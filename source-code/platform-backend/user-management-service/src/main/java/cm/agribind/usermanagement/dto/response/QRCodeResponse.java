package cm.agribind.usermanagement.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QRCodeResponse {

    private String userId;
    private String purpose;
    private String qrCodeImage; // Base64 encoded image
    private String qrCodeData; // Raw data for scanning
    private String downloadUrl;
    private Integer size;
    private String format;
    private LocalDateTime expiresAt;

    public QRCodeResponse(String userId, String purpose, String qrCodeImage, String qrCodeData) {
        this.userId = userId;
        this.purpose = purpose;
        this.qrCodeImage = qrCodeImage;
        this.qrCodeData = qrCodeData;
        this.expiresAt = LocalDateTime.now().plusDays(30);
    }
}