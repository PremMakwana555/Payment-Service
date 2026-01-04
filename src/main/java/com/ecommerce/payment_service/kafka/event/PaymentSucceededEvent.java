package com.ecommerce.payment_service.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSucceededEvent {
    private String paymentId;
    private String orderId;
    private BigDecimal amount;
    private String transactionId;

    // Event metadata
    private String correlationId;
    private String sagaId;
    private String causationId;
    private String timestamp;
    private String source;
}
