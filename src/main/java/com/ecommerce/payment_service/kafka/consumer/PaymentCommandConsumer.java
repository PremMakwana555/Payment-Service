package com.ecommerce.payment_service.kafka.consumer;

import com.ecommerce.payment_service.kafka.event.PaymentRequestCommand;
import com.ecommerce.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCommandConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "${kafka.topics.payments-commands}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void consumePaymentCommand(
            @Payload PaymentRequestCommand command,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(value = "correlationId", required = false) String correlationId,
            @Header(value = "sagaId", required = false) String sagaId,
            Acknowledgment acknowledgment) {
        try {
            log.info("Received PaymentRequestCommand for orderId={}, amount={}, sagaId={}, correlationId={}",
                    command.getOrderId(), command.getAmount(), sagaId, correlationId);

            if (command.getCorrelationId() == null) {
                command.setCorrelationId(correlationId);
            }
            if (command.getSagaId() == null) {
                command.setSagaId(sagaId);
            }

            paymentService.processPayment(command);

            acknowledgment.acknowledge();
            log.info("Successfully processed PaymentRequestCommand for orderId={}, sagaId={}",
                    command.getOrderId(), sagaId);

        } catch (Exception e) {
            log.error("Error processing PaymentRequestCommand for orderId={}, sagaId={}",
                    command.getOrderId(), sagaId, e);
        }
    }
}
