package com.ecommerce.payment_service.service;

import com.ecommerce.payment_service.entity.Payment;
import com.ecommerce.payment_service.entity.PaymentStatus;
import com.ecommerce.payment_service.exception.DuplicatePaymentException;
import com.ecommerce.payment_service.kafka.event.PaymentRequestCommand;
import com.ecommerce.payment_service.paymentGateway.PaymentGateway;
import com.ecommerce.payment_service.paymentGateway.PaymentGatewaySelector;
import com.ecommerce.payment_service.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentGatewaySelector paymentGatewaySelector;

    @Mock
    private PaymentIdGenerator idGenerator;

    @Mock
    private PaymentValidator validator;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private PaymentEventPublisher eventPublisher;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private PaymentService paymentService;

    private PaymentRequestCommand command;
    private Payment payment;

    @BeforeEach
    void setUp() {
        command = PaymentRequestCommand.builder()
                .orderId("order123")
                .amount(java.math.BigDecimal.valueOf(100.0))
                .paymentMethod("CREDIT_CARD")
                .sagaId("saga1")
                .correlationId("corr1")
                .build();

        payment = Payment.builder()
                .paymentId("pay123")
                .orderId("order123")
                .amount(java.math.BigDecimal.valueOf(100.0))
                .status(PaymentStatus.PROCESSING)
                .build();
    }

    @Test
    void processPayment_Success() {
        when(idGenerator.generatePaymentId()).thenReturn("pay123");
        lenient().when(idGenerator.generateTransactionId()).thenReturn("txn123");
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(paymentGatewaySelector.getPaymentGateway(anyString())).thenReturn(paymentGateway);
        when(paymentGateway.generatePaymentUrl()).thenReturn("http://payment.url");
        paymentService.processPayment(command);

        verify(validator).validate(command);
        verify(idempotencyService).checkDuplicatePayment("order123");
        verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
    }

    @Test
    void processPayment_DuplicateException() {
        doThrow(new DuplicatePaymentException("Duplicate")).when(idempotencyService).checkDuplicatePayment(anyString());

        assertThrows(DuplicatePaymentException.class, () -> paymentService.processPayment(command));

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(eventPublisher, never()).publishFailureEvent(any(), any(), any(java.math.BigDecimal.class), any(), any(),
                any());
    }

    @Test
    void initiatePayment_Success() {
        when(paymentGatewaySelector.getPaymentGateway(anyString())).thenReturn(paymentGateway);
        when(paymentGateway.generatePaymentUrl()).thenReturn("http://url");

        String url = paymentService.initiatePayment("order1");
        assertEquals("http://url", url);
    }
}
