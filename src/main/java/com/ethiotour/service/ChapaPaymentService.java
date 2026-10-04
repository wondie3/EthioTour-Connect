package com.ethiotour.service;

import com.ethiotour.config.DatabaseConfig;
import com.ethiotour.model.Booking;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChapaPaymentService {
    private final HttpClient httpClient;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

    public ChapaPaymentService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public ChapaPaymentResult initializeCheckout(Booking booking) {
        String secretKey = getSecretKey();
        if (secretKey.isBlank()) {
            return ChapaPaymentResult.failed("Chapa secret key is missing.", null, null);
        }

        String txRef = createTransactionReference(booking);
        String payload = buildInitializePayload(booking, txRef);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(DatabaseConfig.getChapaInitializeUrl()))
            .timeout(REQUEST_TIMEOUT)
            .header("Authorization", "Bearer " + secretKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            String status = extractJsonString(body, "status");
            String message = extractChapaMessage(body);
            String checkoutUrl = extractJsonString(body, "checkout_url");

            if (response.statusCode() >= 200 && response.statusCode() < 300
                && "success".equalsIgnoreCase(status) && checkoutUrl != null) {
                return ChapaPaymentResult.initialized(
                    message != null ? message : "Chapa checkout initialized.",
                    txRef,
                    checkoutUrl
                );
            }

            return ChapaPaymentResult.failed(
                message != null ? message : "Chapa checkout initialization failed. HTTP " + response.statusCode(),
                txRef,
                status
            );
        } catch (IOException e) {
            return ChapaPaymentResult.failed("Could not reach Chapa: " + e.getMessage(), txRef, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ChapaPaymentResult.failed("Chapa request was interrupted.", txRef, null);
        }
    }

    public ChapaPaymentResult verifyPayment(String txRef) {
        String secretKey = getSecretKey();
        if (secretKey.isBlank()) {
            return ChapaPaymentResult.failed("Chapa secret key is missing.", txRef, null);
        }
        if (txRef == null || txRef.isBlank()) {
            return ChapaPaymentResult.failed("Missing Chapa transaction reference.", txRef, null);
        }

        String encodedRef = URLEncoder.encode(txRef, StandardCharsets.UTF_8);
        String verifyUrl = DatabaseConfig.getChapaVerifyUrl() + "/" + encodedRef;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(verifyUrl))
            .timeout(REQUEST_TIMEOUT)
            .header("Authorization", "Bearer " + secretKey)
            .GET()
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            String status = extractJsonString(body, "status");
            String message = extractChapaMessage(body);

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return ChapaPaymentResult.verified(
                    message != null ? message : "Chapa verification complete.",
                    txRef,
                    status
                );
            }

            return ChapaPaymentResult.failed(
                message != null ? message : "Chapa verification failed. HTTP " + response.statusCode(),
                txRef,
                status
            );
        } catch (IOException e) {
            return ChapaPaymentResult.failed("Could not reach Chapa: " + e.getMessage(), txRef, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ChapaPaymentResult.failed("Chapa verification was interrupted.", txRef, null);
        }
    }

    private String getSecretKey() {
        String envKey = System.getenv("CHAPA_SECRET_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey.trim();
        }
        return DatabaseConfig.getChapaSecretKey().trim();
    }

    private String buildInitializePayload(Booking booking, String txRef) {
        String[] names = splitName(booking.getCustomerName());
        String amount = BigDecimal.valueOf(booking.getTotalPrice()).setScale(2, RoundingMode.HALF_UP).toPlainString();
        String phone = normalizePhone(booking.getCustomerPhone());

        StringBuilder payload = new StringBuilder("{"
            + "\"amount\":\"" + jsonEscape(amount) + "\","
            + "\"currency\":\"" + jsonEscape(DatabaseConfig.getChapaCurrency()) + "\","
            + "\"email\":\"" + jsonEscape(booking.getCustomerEmail()) + "\","
            + "\"first_name\":\"" + jsonEscape(names[0]) + "\","
            + "\"last_name\":\"" + jsonEscape(names[1]) + "\","
            + "\"tx_ref\":\"" + jsonEscape(txRef) + "\",");

        if (!phone.isBlank()) {
            payload.append("\"phone_number\":\"").append(jsonEscape(phone)).append("\",");
        }

        payload.append(
            "\"return_url\":\"" + jsonEscape(DatabaseConfig.getChapaReturnUrl()) + "\","
            + "\"customization\":{"
            + "\"title\":\"" + jsonEscape(DatabaseConfig.getChapaTitle()) + "\","
            + "\"description\":\"Tour booking #" + booking.getId() + "\""
            + "}"
            + "}");

        return payload.toString();
    }

    private String createTransactionReference(Booking booking) {
        return "ETHIOTOUR-" + booking.getId() + "-" + Instant.now().toEpochMilli();
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return new String[] { "EthioTour", "Guest" };
        }

        String[] parts = fullName.trim().split("\\s+", 2);
        if (parts.length == 1) {
            return new String[] { parts[0], "Guest" };
        }
        return parts;
    }

    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.startsWith("251") && digits.length() == 12) {
            digits = "0" + digits.substring(3);
        }
        if (digits.matches("0[79][0-9]{8}")) {
            return digits;
        }
        return "";
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r");
    }

    private String extractJsonString(String json, String key) {
        if (json == null || key == null) {
            return null;
        }

        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1)
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\n", "\n")
            .replace("\\r", "\r");
    }

    private String extractChapaMessage(String json) {
        String message = extractJsonString(json, "message");
        if (message != null) {
            return message;
        }

        if (json == null) {
            return null;
        }

        Matcher matcher = Pattern.compile("\"message\"\\s*:\\s*\\{(.*?)\\}\\s*,\\s*\"status\"", Pattern.DOTALL).matcher(json);
        if (!matcher.find()) {
            return null;
        }

        String messageObject = matcher.group(1);
        Matcher fieldMatcher = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\[(.*?)\\]").matcher(messageObject);
        StringBuilder details = new StringBuilder();
        while (fieldMatcher.find()) {
            if (details.length() > 0) {
                details.append("; ");
            }
            details.append(fieldMatcher.group(1)).append(": ")
                .append(fieldMatcher.group(2).replace("\"", "").trim());
        }
        return details.length() > 0 ? details.toString() : null;
    }
}
