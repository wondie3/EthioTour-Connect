package com.ethiotour.service;

public class ChapaPaymentResult {
    private final boolean success;
    private final String message;
    private final String txRef;
    private final String checkoutUrl;
    private final String status;

    private ChapaPaymentResult(boolean success, String message, String txRef, String checkoutUrl, String status) {
        this.success = success;
        this.message = message;
        this.txRef = txRef;
        this.checkoutUrl = checkoutUrl;
        this.status = status;
    }

    public static ChapaPaymentResult initialized(String message, String txRef, String checkoutUrl) {
        return new ChapaPaymentResult(true, message, txRef, checkoutUrl, "initialized");
    }

    public static ChapaPaymentResult verified(String message, String txRef, String status) {
        return new ChapaPaymentResult("success".equalsIgnoreCase(status), message, txRef, null, status);
    }

    public static ChapaPaymentResult failed(String message, String txRef, String status) {
        return new ChapaPaymentResult(false, message, txRef, null, status);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getTxRef() {
        return txRef;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public String getStatus() {
        return status;
    }
}
