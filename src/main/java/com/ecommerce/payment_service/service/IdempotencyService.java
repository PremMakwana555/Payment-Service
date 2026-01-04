package com.ecommerce.payment_service.service;

import com.ecommerce.payment_service.exception.DuplicatePaymentException;
import com.ecommerce.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final PaymentRepository paymentRepository;

    public void checkDuplicatePayment(String orderId) {
        paymentRepository.findByOrderId(orderId).ifPresent(existingPayment -> {
            log.warn("Duplicate payment attempt detected for orderId={}, existing paymentId={}",
                    orderId, existingPayment.getPaymentId());
            throw new DuplicatePaymentException(orderId);
        });
    }
}
