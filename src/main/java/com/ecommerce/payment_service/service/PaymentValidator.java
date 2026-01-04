package com.ecommerce.payment_service.service;

import com.ecommerce.payment_service.kafka.event.PaymentRequestCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class PaymentValidator {

    public void validate(PaymentRequestCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Payment command cannot be null");
        }

        if (command.getOrderId() == null || command.getOrderId().isBlank()) {
            throw new IllegalArgumentException("Order ID is required");
        }

        if (command.getAmount() == null || command.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        if (command.getSagaId() == null || command.getSagaId().isBlank()) {
            throw new IllegalArgumentException("Saga ID is required for payment processing");
        }

        log.debug("Payment command validation passed for orderId={}", command.getOrderId());
    }
}
