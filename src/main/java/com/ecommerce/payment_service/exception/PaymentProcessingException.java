package com.ecommerce.payment_service.exception;

public class PaymentProcessingException extends RuntimeException {
    private final String orderId;
    private final String reason;

    public PaymentProcessingException(String orderId, String reason) {
        super(String.format("Payment processing failed for order %s: %s", orderId, reason));
        this.orderId = orderId;
        this.reason = reason;
    }

    public PaymentProcessingException(String orderId, String reason, Throwable cause) {
        super(String.format("Payment processing failed for order %s: %s", orderId, reason), cause);
        this.orderId = orderId;
        this.reason = reason;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getReason() {
        return reason;
    }
}
