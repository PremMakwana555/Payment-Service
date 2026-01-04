package com.ecommerce.payment_service.service;

import com.ecommerce.payment_service.kafka.event.PaymentFailedEvent;
import com.ecommerce.payment_service.kafka.event.PaymentSucceededEvent;
import com.ecommerce.payment_service.kafka.producer.PaymentEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final PaymentEventProducer paymentEventProducer;

    public void publishSuccessEvent(String paymentId, String orderId, BigDecimal amount,
            String transactionId, String correlationId,
            String sagaId) {
        PaymentSucceededEvent event = PaymentSucceededEvent.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .amount(amount)
                .transactionId(transactionId)
                .correlationId(correlationId)
                .sagaId(sagaId)
                .causationId(orderId)
                .build();

        paymentEventProducer.publishPaymentSucceededEvent(event);
        log.info("Published PaymentSucceededEvent for orderId={}, paymentId={}", orderId, paymentId);
    }

    public void publishFailureEvent(String paymentId, String orderId, BigDecimal amount,
            String failureReason, String correlationId,
            String sagaId) {
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .amount(amount)
                .failureReason(failureReason)
                .correlationId(correlationId)
                .sagaId(sagaId)
                .causationId(orderId)
                .build();

        paymentEventProducer.publishPaymentFailedEvent(event);
        log.warn("Published PaymentFailedEvent for orderId={}, paymentId={}, reason={}",
                orderId, paymentId, failureReason);
    }
}
