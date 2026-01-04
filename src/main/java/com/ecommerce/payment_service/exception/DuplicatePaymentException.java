package com.ecommerce.payment_service.exception;

public class DuplicatePaymentException extends RuntimeException {
    private final String orderId;

    public DuplicatePaymentException(String orderId) {
        super(String.format("Payment already processed for order %s", orderId));
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }
}
