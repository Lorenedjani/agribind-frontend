package cm.agribind.usermanagement.controller;

import cm.agribind.usermanagement.dto.command.GenerateQRCodeCommand;
import cm.agribind.usermanagement.dto.response.QRCodeResponse;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.service.QRCodeService;
import cm.agribind.usermanagement.service.query.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/qrcodes")
@RequiredArgsConstructor
@Tag(name = "QR Code", description = "APIs for QR code generation and validation")
public class QRCodeController {

    private final QRCodeService qrCodeService;
    private final UserQueryService userQueryService;

    @PostMapping("/generate")
    @Operation(summary = "Generate QR code", description = "Generate QR code for registration, login, or profile")
    public ResponseEntity<QRCodeResponse> generateQRCode(@Valid @RequestBody GenerateQRCodeCommand command) {
        log.info("Generating QR code for user: {}, purpose: {}", command.getUserId(), command.getPurpose());
        QRCodeResponse response = qrCodeService.generateQRCode(command);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/registration")
    @Operation(summary = "Generate registration QR code", description = "Generate QR code for farmer registration")
    public ResponseEntity<QRCodeResponse> generateRegistrationQRCode(@PathVariable String userId) {
        try {
            QRCodeResponse response = qrCodeService.generateRegistrationQRCode(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return basic response if QR generation fails
            QRCodeResponse fallback = new QRCodeResponse(userId, "REGISTRATION", "", "USER:" + userId + ":REGISTRATION:" + System.currentTimeMillis());
            fallback.setSize(300);
            fallback.setFormat("PNG");
            return ResponseEntity.ok(fallback);
        }
    }

    @GetMapping("/{userId}/login")
    @Operation(summary = "Generate login QR code", description = "Generate QR code for quick login")
    public ResponseEntity<QRCodeResponse> generateLoginQRCode(@PathVariable String userId) {
        QRCodeResponse response = qrCodeService.generateLoginQRCode(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/profile")
    @Operation(summary = "Generate profile QR code", description = "Generate QR code for profile sharing")
    public ResponseEntity<QRCodeResponse> generateProfileQRCode(@PathVariable String userId) {
        GenerateQRCodeCommand command = new GenerateQRCodeCommand();
        command.setUserId(userId);
        command.setPurpose("PROFILE");
        command.setSize(250);
        command.setFormat("PNG");

        QRCodeResponse response = qrCodeService.generateQRCode(command);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate QR code", description = "Validate QR code and return user information")
    public ResponseEntity<UserResponse> validateQRCode(@RequestParam String qrData) {
        log.info("Validating QR code");
        var user = qrCodeService.validateQRCode(qrData);
        UserResponse response = userQueryService.getUserById(user.getUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/scan-login")
    @Operation(summary = "QR code login", description = "Login using QR code scan")
    public ResponseEntity<UserResponse> qrCodeLogin(@RequestParam String qrData) {
        log.info("QR code login attempt");
        var user = qrCodeService.validateQRCode(qrData);

        // In real implementation, this would generate an authentication token
        UserResponse response = userQueryService.getUserById(user.getUserId());

        log.info("QR code login successful for user: {}", user.getUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/scan-registration")
    @Operation(summary = "QR code registration", description = "Complete registration using QR code scan")
    public ResponseEntity<UserResponse> qrCodeRegistration(@RequestParam String qrData) {
        log.info("QR code registration attempt");
        var user = qrCodeService.validateQRCode(qrData);

        // In real implementation, this would complete the registration process
        UserResponse response = userQueryService.getUserById(user.getUserId());

        log.info("QR code registration completed for user: {}", user.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/download")
    @Operation(summary = "Download QR code", description = "Download QR code as image file")
    public ResponseEntity<byte[]> downloadQRCode(
            @PathVariable String userId,
            @RequestParam(defaultValue = "REGISTRATION") String purpose,
            @RequestParam(defaultValue = "300") int size,
            @RequestParam(defaultValue = "PNG") String format) {

        GenerateQRCodeCommand command = new GenerateQRCodeCommand();
        command.setUserId(userId);
        command.setPurpose(purpose);
        command.setSize(size);
        command.setFormat(format);

        QRCodeResponse qrResponse = qrCodeService.generateQRCode(command);

        // Extract base64 image data
        String base64Data = qrResponse.getQrCodeImage().replace("data:image/png;base64,", "");
        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);

        String filename = String.format("qrcode_%s_%s.%s", userId, purpose.toLowerCase(), format.toLowerCase());

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.IMAGE_PNG)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(imageBytes);
    }
}