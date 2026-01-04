package com.ecommerce.payment_service.controllers;

import com.ecommerce.payment_service.paymentGateway.PaymentGateway;
import com.ecommerce.payment_service.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/payment")
    public String initiatePayment(@RequestBody String orderId) {
        return paymentService.initiatePayment(orderId);
    }
}
