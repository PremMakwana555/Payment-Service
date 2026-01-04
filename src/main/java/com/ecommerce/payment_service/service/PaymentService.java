package com.ecommerce.payment_service.service;

import com.ecommerce.payment_service.entity.Payment;
import com.ecommerce.payment_service.entity.PaymentStatus;
import com.ecommerce.payment_service.exception.DuplicatePaymentException;
import com.ecommerce.payment_service.exception.PaymentProcessingException;
import com.ecommerce.payment_service.kafka.event.PaymentRequestCommand;
import com.ecommerce.payment_service.paymentGateway.PaymentGateway;
import com.ecommerce.payment_service.paymentGateway.PaymentGatewaySelector;
import com.ecommerce.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentProcessor {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewaySelector paymentGatewaySelector;
    private final PaymentIdGenerator idGenerator;
    private final PaymentValidator validator;
    private final IdempotencyService idempotencyService;
    private final PaymentEventPublisher eventPublisher;

    @Override
    @Transactional
    public void processPayment(PaymentRequestCommand command) {
        log.info("Processing payment for orderId={}, amount={}, sagaId={}",
                command.getOrderId(), command.getAmount(), command.getSagaId());

        try {
            validator.validate(command);
            idempotencyService.checkDuplicatePayment(command.getOrderId());
            String paymentId = idGenerator.generatePaymentId();

            Payment payment = createPaymentRecord(paymentId, command);
            payment = paymentRepository.save(payment);

            boolean success = processWithRetry(command);

            if (success) {
                handleSuccessfulPayment(payment, command);
            } else {
                handleFailedPayment(payment, "Payment declined by gateway", command);
            }

        } catch (DuplicatePaymentException e) {
            log.warn("Duplicate payment detected: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("Invalid payment command: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing payment for orderId={}",
                    command.getOrderId(), e);
            throw new PaymentProcessingException(command.getOrderId(), e.getMessage(), e);
        }
    }

    private Payment createPaymentRecord(String paymentId, PaymentRequestCommand command) {
        return Payment.builder()
                .paymentId(paymentId)
                .orderId(command.getOrderId())
                .amount(command.getAmount())
                .status(PaymentStatus.PROCESSING)
                .paymentMethod(command.getPaymentMethod())
                .build();
    }

    private boolean processWithRetry(PaymentRequestCommand command) {
        try {
            PaymentGateway gateway = paymentGatewaySelector.getPaymentGateway(command.getPaymentMethod());
            String paymentUrl = gateway.generatePaymentUrl();

            boolean success = Math.random() < 0.8;

            log.debug("Payment gateway processing for orderId={}: success={}",
                    command.getOrderId(), success);

            return success;

        } catch (Exception e) {
            log.error("Payment gateway error for orderId={}", command.getOrderId(), e);
            throw new PaymentProcessingException(command.getOrderId(),
                    "Gateway communication failed", e);
        }
    }

    private void handleSuccessfulPayment(Payment payment, PaymentRequestCommand command) {
        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setTransactionId(idGenerator.generateTransactionId());
        paymentRepository.save(payment);

        log.info("Payment succeeded: paymentId={}, orderId={}, sagaId={}",
                payment.getPaymentId(), command.getOrderId(), command.getSagaId());

        eventPublisher.publishSuccessEvent(
                payment.getPaymentId(),
                command.getOrderId(),
                command.getAmount(),
                payment.getTransactionId(),
                command.getCorrelationId(),
                command.getSagaId());
    }

    private void handleFailedPayment(Payment payment, String reason, PaymentRequestCommand command) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        paymentRepository.save(payment);

        log.warn("Payment failed: paymentId={}, orderId={}, sagaId={}, reason={}",
                payment.getPaymentId(), command.getOrderId(), command.getSagaId(), reason);

        eventPublisher.publishFailureEvent(
                payment.getPaymentId(),
                command.getOrderId(),
                command.getAmount(),
                reason,
                command.getCorrelationId(),
                command.getSagaId());
    }

    public String initiatePayment(String orderId) {
        try {
            // Default to STRIPE for simple initiation
            PaymentGateway gateway = paymentGatewaySelector.getPaymentGateway("stripe");
            return gateway.generatePaymentUrl();
        } catch (Exception e) {
            log.error("Error generating payment URL for orderId={}", orderId, e);
            throw new PaymentProcessingException(orderId, "Failed to generate payment URL", e);
        }
    }
}
