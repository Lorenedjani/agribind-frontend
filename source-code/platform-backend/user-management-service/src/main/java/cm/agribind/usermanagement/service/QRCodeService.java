package cm.agribind.usermanagement.service;

import cm.agribind.usermanagement.dto.command.GenerateQRCodeCommand;
import cm.agribind.usermanagement.dto.response.QRCodeResponse;
import cm.agribind.usermanagement.entity.User;

public interface QRCodeService {

    QRCodeResponse generateQRCode(GenerateQRCodeCommand command);
    QRCodeResponse generateRegistrationQRCode(String userId);
    QRCodeResponse generateLoginQRCode(String userId);
    User validateQRCode(String qrData);
    String generateQRData(User user, String purpose);
    String generateRegistrationData(User user);
}