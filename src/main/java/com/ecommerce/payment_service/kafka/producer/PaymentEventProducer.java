package com.ecommerce.payment_service.kafka.producer;

import com.ecommerce.payment_service.kafka.event.PaymentFailedEvent;
import com.ecommerce.payment_service.kafka.event.PaymentSucceededEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.payments-events}")
    private String paymentsEventsTopic;

    public void publishPaymentSucceededEvent(PaymentSucceededEvent event) {
        try {
            // Set event metadata
            event.setTimestamp(Instant.now().toString());
            event.setSource("payment-service");

            // Create producer record with headers
            ProducerRecord<String, Object> record = new ProducerRecord<>(
                    paymentsEventsTopic,
                    event.getOrderId(),
                    event);

            // Add headers as per PRD requirements
            record.headers()
                    .add(new RecordHeader("correlationId", event.getCorrelationId().getBytes(StandardCharsets.UTF_8)));
            record.headers().add(new RecordHeader("sagaId", event.getSagaId().getBytes(StandardCharsets.UTF_8)));
            record.headers()
                    .add(new RecordHeader("causationId", event.getCausationId().getBytes(StandardCharsets.UTF_8)));
            record.headers().add(new RecordHeader("timestamp", event.getTimestamp().getBytes(StandardCharsets.UTF_8)));
            record.headers().add(new RecordHeader("source", "payment-service".getBytes(StandardCharsets.UTF_8)));
            record.headers().add(new RecordHeader("eventType", "PaymentSucceeded".getBytes(StandardCharsets.UTF_8)));

            // Send to Kafka
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(record);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info(
                            "PaymentSucceededEvent published successfully for orderId={}, paymentId={}, sagaId={}, offset={}",
                            event.getOrderId(), event.getPaymentId(), event.getSagaId(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish PaymentSucceededEvent for orderId={}, paymentId={}, sagaId={}",
                            event.getOrderId(), event.getPaymentId(), event.getSagaId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing PaymentSucceededEvent for orderId={}, paymentId={}",
                    event.getOrderId(), event.getPaymentId(), e);
        }
    }

    public void publishPaymentFailedEvent(PaymentFailedEvent event) {
        try {
            // Set event metadata
            event.setTimestamp(Instant.now().toString());
            event.setSource("payment-service");

            // Create producer record with headers
            ProducerRecord<String, Object> record = new ProducerRecord<>(
                    paymentsEventsTopic,
                    event.getOrderId(),
                    event);

            // Add headers as per PRD requirements
            record.headers()
                    .add(new RecordHeader("correlationId", event.getCorrelationId().getBytes(StandardCharsets.UTF_8)));
            record.headers().add(new RecordHeader("sagaId", event.getSagaId().getBytes(StandardCharsets.UTF_8)));
            record.headers()
                    .add(new RecordHeader("causationId", event.getCausationId().getBytes(StandardCharsets.UTF_8)));
            record.headers().add(new RecordHeader("timestamp", event.getTimestamp().getBytes(StandardCharsets.UTF_8)));
            record.headers().add(new RecordHeader("source", "payment-service".getBytes(StandardCharsets.UTF_8)));
            record.headers().add(new RecordHeader("eventType", "PaymentFailed".getBytes(StandardCharsets.UTF_8)));

            // Send to Kafka
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(record);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info(
                            "PaymentFailedEvent published successfully for orderId={}, paymentId={}, sagaId={}, reason={}, offset={}",
                            event.getOrderId(), event.getPaymentId(), event.getSagaId(), event.getFailureReason(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish PaymentFailedEvent for orderId={}, paymentId={}, sagaId={}",
                            event.getOrderId(), event.getPaymentId(), event.getSagaId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error publishing PaymentFailedEvent for orderId={}, paymentId={}, reason={}",
                    event.getOrderId(), event.getPaymentId(), event.getFailureReason(), e);
        }
    }
}
