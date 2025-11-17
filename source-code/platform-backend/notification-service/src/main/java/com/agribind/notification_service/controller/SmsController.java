package com.agribind.notification_service.controller;

import com.agribind.notification_service.service.SmsService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/notifications/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    @PostMapping("/send")
    public ResponseEntity<SmsResponse> sendSms(@RequestBody SmsRequest request) {
        log.info("Received SMS request for: {}", request.getPhoneNumber());

        try {
            smsService.sendSms(request.getPhoneNumber(), request.getMessage());

            return ResponseEntity.ok(new SmsResponse(
                    true,
                    "SMS sent successfully",
                    request.getPhoneNumber()
            ));
        } catch (Exception e) {
            log.error("SMS sending failed", e);
            return ResponseEntity.ok(new SmsResponse(
                    false,
                    "SMS sending failed: " + e.getMessage(),
                    request.getPhoneNumber()
            ));
        }
    }

    @Data
    public static class SmsRequest {
        private String phoneNumber;
        private String message;
        private String type;
        private String priority;
    }

    @Data
    public static class SmsResponse {
        private final boolean success;
        private final String message;
        private final String phoneNumber;
    }
}