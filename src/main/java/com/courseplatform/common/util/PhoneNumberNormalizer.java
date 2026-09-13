package com.courseplatform.common.util;

import org.springframework.util.StringUtils;

public final class PhoneNumberNormalizer {

    private PhoneNumberNormalizer() {
        // Utility class
    }

    /**
     * Normalizes a raw phone number into a standard canonical format (+E.164-style).
     * Examples:
     * - "9876543210" -> "+919876543210"
     * - "09876543210" -> "+919876543210"
     * - "+91 98765-43210" -> "+919876543210"
     * - "+1 (555) 019-2834" -> "+15550192834"
     *
     * @param rawPhone Raw phone input
     * @return Canonical normalized phone number or null
     */
    public static String normalize(String rawPhone) {
        if (!StringUtils.hasText(rawPhone)) {
            return null;
        }

        String cleaned = rawPhone.replaceAll("[\\s\\-().]", "").trim();
        if (cleaned.isEmpty()) {
            return null;
        }

        // Leading 0 stripping (e.g. 09876543210 -> 9876543210)
        if (cleaned.startsWith("0") && cleaned.length() == 11) {
            cleaned = cleaned.substring(1);
        }

        // Standard 10-digit Indian mobile number
        if (cleaned.matches("^[6-9]\\d{9}$")) {
            return "+91" + cleaned;
        }

        // 12-digit Indian number without +
        if (cleaned.matches("^91[6-9]\\d{9}$")) {
            return "+" + cleaned;
        }

        // International format starting with +
        if (cleaned.startsWith("+")) {
            String digits = cleaned.substring(1);
            if (digits.matches("^\\d{7,15}$")) {
                return cleaned;
            }
        }

        // Digits only between 7 and 15 digits
        if (cleaned.matches("^\\d{7,15}$")) {
            return "+" + cleaned;
        }

        return cleaned;
    }
}
