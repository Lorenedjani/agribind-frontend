package cm.agribind.usermanagement.dto.command;

import lombok.Data;

@Data
public class GenerateQRCodeCommand {

    private String userId;
    private String purpose; // REGISTRATION, LOGIN, PROFILE
    private Integer size = 300; // QR code size in pixels
    private String format = "PNG"; // PNG, JPEG, SVG
}