package com.agribind.communication.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility class for phone number validation and formatting
 * Specifically handles Cameroon phone numbers
 */
@Slf4j
@Component
public class PhoneNumberUtil {

    // Cameroon country code
    private static final String CAMEROON_COUNTRY_CODE = "+237";

    // Cameroon phone number pattern (9 digits starting with 6)
    private static final Pattern CAMEROON_PATTERN = Pattern.compile("^6\\d{8}$");

    // International format pattern
    private static final Pattern INTERNATIONAL_PATTERN = Pattern.compile("^\\+237\\d{9}$");

    /**
     * Validate if phone number is valid Cameroon format
     *
     * @param phoneNumber Phone number to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidCameroonNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        String cleaned = cleanPhoneNumber(phoneNumber);

        // Check if it matches 9-digit format
        if (CAMEROON_PATTERN.matcher(cleaned).matches()) {
            return true;
        }

        // Check if it matches international format
        if (INTERNATIONAL_PATTERN.matcher(phoneNumber).matches()) {
            return true;
        }

        log.warn("Invalid Cameroon phone number format: {}", phoneNumber);
        return false;
    }

    /**
     * Format phone number to international format (+237XXXXXXXXX)
     *
     * @param phoneNumber Phone number to format
     * @return Formatted phone number
     */
    public String formatToInternational(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }

        String cleaned = cleanPhoneNumber(phoneNumber);

        // If already in international format, return as is
        if (phoneNumber.startsWith(CAMEROON_COUNTRY_CODE)) {
            return phoneNumber;
        }

        // If 9 digits starting with 6, add country code
        if (CAMEROON_PATTERN.matcher(cleaned).matches()) {
            return CAMEROON_COUNTRY_CODE + cleaned;
        }

        log.error("Cannot format invalid phone number: {}", phoneNumber);
        return phoneNumber; // Return original if cannot format
    }

    /**
     * Format phone number to local format (6XXXXXXXX)
     *
     * @param phoneNumber Phone number to format
     * @return Formatted phone number
     */
    public String formatToLocal(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return null;
        }

        // Remove country code if present
        if (phoneNumber.startsWith(CAMEROON_COUNTRY_CODE)) {
            return phoneNumber.substring(CAMEROON_COUNTRY_CODE.length());
        }

        return cleanPhoneNumber(phoneNumber);
    }

    /**
     * Clean phone number by removing all non-digit characters except +
     *
     * @param phoneNumber Phone number to clean
     * @return Cleaned phone number
     */
    public String cleanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        // Keep + for international format, remove everything else except digits
        return phoneNumber.replaceAll("[^0-9+]", "").trim();
    }

    /**
     * Normalize phone number to E.164 format for SMS sending
     *
     * @param phoneNumber Phone number to normalize
     * @return Normalized phone number in E.164 format
     */
    public String normalizeForSms(String phoneNumber) {
        return formatToInternational(phoneNumber);
    }

    /**
     * Extract country code from phone number
     *
     * @param phoneNumber Phone number
     * @return Country code or null
     */
    public String extractCountryCode(String phoneNumber) {
        if (phoneNumber != null && phoneNumber.startsWith("+")) {
            // Cameroon country code is always +237 (4 characters)
            if (phoneNumber.startsWith(CAMEROON_COUNTRY_CODE)) {
                return CAMEROON_COUNTRY_CODE;
            }
        }
        return null;
    }

    /**
     * Check if phone number is in international format
     *
     * @param phoneNumber Phone number to check
     * @return true if international format, false otherwise
     */
    public boolean isInternationalFormat(String phoneNumber) {
        return phoneNumber != null && phoneNumber.startsWith("+");
    }

    /**
     * Mask phone number for display (e.g., +237 6XX XXX XXX)
     *
     * @param phoneNumber Phone number to mask
     * @return Masked phone number
     */
    public String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 9) {
            return phoneNumber;
        }

        String formatted = formatToInternational(phoneNumber);
        if (formatted == null || formatted.length() < 12) {
            return phoneNumber;
        }

        // Format: +237 6XX XXX XXX
        return formatted.substring(0, 6) + "XX XXX XXX";
    }

    /**
     * Format phone number for display with spaces
     *
     * @param phoneNumber Phone number to format
     * @return Formatted phone number (e.g., +237 6 12 34 56 78)
     */
    public String formatForDisplay(String phoneNumber) {
        String formatted = formatToInternational(phoneNumber);
        if (formatted == null || formatted.length() != 13) {
            return phoneNumber;
        }

        // Format: +237 6 12 34 56 78
        return String.format("%s %s %s %s %s %s",
                formatted.substring(0, 4),  // +237
                formatted.substring(4, 5),  // 6
                formatted.substring(5, 7),  // 12
                formatted.substring(7, 9),  // 34
                formatted.substring(9, 11), // 56
                formatted.substring(11, 13) // 78
        );
    }

    /**
     * Validate and format a phone number
     *
     * @param phoneNumber Phone number to validate and format
     * @return Formatted phone number or null if invalid
     */
    public String validateAndFormat(String phoneNumber) {
        if (isValidCameroonNumber(phoneNumber)) {
            return formatToInternational(phoneNumber);
        }
        return null;
    }
}