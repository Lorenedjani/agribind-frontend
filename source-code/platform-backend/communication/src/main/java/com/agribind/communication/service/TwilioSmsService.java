package com.agribind.communication.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class TwilioSmsService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    @Value("${communication.sms.cost-per-message}")
    private Double costPerMessage;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        log.info("Twilio SMS Service initialized");
    }

    /**
     * Send a single SMS message
     */
    public boolean sendSms(String toPhoneNumber, String messageContent) {
        try {
            Message message = Message.creator(
                new PhoneNumber(toPhoneNumber),
                new PhoneNumber(fromPhoneNumber),
                messageContent
            ).create();

            log.info("SMS sent successfully to {} with SID: {}", toPhoneNumber, message.getSid());
            return true;
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", toPhoneNumber, e.getMessage());
            return false;
        }
    }

    /**
     * Send bulk SMS messages asynchronously
     */
    public CompletableFuture<BulkSmsResult> sendBulkSms(
        List<String> phoneNumbers,
        String messageContent
    ) {
        return CompletableFuture.supplyAsync(() -> {
            int successCount = 0;
            int failureCount = 0;
            List<String> failedNumbers = new ArrayList<>();

            for (String phoneNumber : phoneNumbers) {
                try {
                    boolean success = sendSms(phoneNumber, messageContent);
                    if (success) {
                        successCount++;
                    } else {
                        failureCount++;
                        failedNumbers.add(phoneNumber);
                    }

                    // Rate limiting - wait between messages
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    log.error("Bulk SMS sending interrupted", e);
                    Thread.currentThread().interrupt();
                    failureCount++;
                    failedNumbers.add(phoneNumber);
                }
            }

            double totalCost = successCount * costPerMessage;

            return BulkSmsResult.builder()
                .totalSent(phoneNumbers.size())
                .successCount(successCount)
                .failureCount(failureCount)
                .failedNumbers(failedNumbers)
                .totalCost(totalCost)
                .build();
        });
    }

    /**
     * Calculate estimated cost for bulk SMS
     */
    public double calculateEstimatedCost(int recipientCount) {
        return recipientCount * costPerMessage;
    }

    /**
     * Validate phone number format for Cameroon
     */
    public boolean isValidCameroonPhoneNumber(String phoneNumber) {
        // Cameroon phone numbers: +237XXXXXXXXX (9 digits after country code)
        return phoneNumber != null &&
               phoneNumber.matches("^\\+237[6-9][0-9]{8}$");
    }

    /**
     * Format phone number to international format
     */
    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;

        // Remove spaces and dashes
        phoneNumber = phoneNumber.replaceAll("[\\s-]", "");

        // Add country code if missing
        if (!phoneNumber.startsWith("+237")) {
            if (phoneNumber.startsWith("237")) {
                phoneNumber = "+" + phoneNumber;
            } else if (phoneNumber.startsWith("6") || phoneNumber.startsWith("7")) {
                phoneNumber = "+237" + phoneNumber;
            }
        }

        return phoneNumber;
    }
}
