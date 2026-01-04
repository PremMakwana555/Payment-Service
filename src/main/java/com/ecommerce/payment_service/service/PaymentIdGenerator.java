package com.ecommerce.payment_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class PaymentIdGenerator {

    private static final String PAYMENT_PREFIX = "PAY-";
    private static final int ID_LENGTH = 13;

    public String generatePaymentId() {
        String id = PAYMENT_PREFIX + UUID.randomUUID().toString()
                .substring(0, ID_LENGTH)
                .toUpperCase();
        log.debug("Generated payment ID: {}", id);
        return id;
    }

    public String generateTransactionId() {
        String txnId = "TXN-" + UUID.randomUUID().toString()
                .substring(0, ID_LENGTH)
                .toUpperCase();
        log.debug("Generated transaction ID: {}", txnId);
        return txnId;
    }
}
