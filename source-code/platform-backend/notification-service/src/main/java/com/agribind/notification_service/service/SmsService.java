package com.agribind.notification_service.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        log.info("Twilio SMS service initialized");
    }

    public void sendSms(String toPhoneNumber, String messageBody) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(formatPhoneNumber(toPhoneNumber)),
                    new PhoneNumber(fromPhoneNumber),
                    messageBody
            ).create();

            log.info("SMS sent successfully. SID: {}, To: {}", message.getSid(), toPhoneNumber);
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", toPhoneNumber, e);
            throw new RuntimeException("SMS sending failed", e);
        }
    }

    private String formatPhoneNumber(String phone) {
        // Ensure phone number starts with country code
        if (!phone.startsWith("+")) {
            // Assuming Cameroon (+237)
            return "+237" + phone.replaceAll("[^0-9]", "");
        }
        return phone;
    }
}