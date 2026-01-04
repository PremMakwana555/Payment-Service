package com.ecommerce.payment_service.service;

import com.ecommerce.payment_service.kafka.event.PaymentRequestCommand;

public interface IPaymentProcessor {
    void processPayment(PaymentRequestCommand command);
}
