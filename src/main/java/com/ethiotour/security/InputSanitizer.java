package com.ethiotour.security;

import java.util.regex.Pattern;

/**
 * Utility for input sanitization and validation across application models and services.
 * Helps prevent injection attacks, cross-site scripting (XSS), and data corruption.
 */
public class InputSanitizer {
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^\\+?[0-9\\s\\-()]{7,20}$");

    private static final Pattern HTML_TAG_PATTERN =
        Pattern.compile("<[^>]*>");

    /**
     * Sanitizes string input by stripping HTML tags and trimming whitespace.
     */
    public static String sanitizeString(String input) {
        if (input == null) {
            return "";
        }
        String stripped = HTML_TAG_PATTERN.matcher(input).replaceAll("");
        return stripped.trim();
    }

    /**
     * Validates whether an email string is well-formed.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates whether a phone number string is valid.
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Sanitizes an email input string.
     */
    public static String sanitizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return sanitizeString(email).toLowerCase();
    }

    /**
     * Sanitizes a phone number input string.
     */
    public static String sanitizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replaceAll("[^0-9+]", "").trim();
    }
}
