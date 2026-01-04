package com.ecommerce.payment_service.paymentGateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewaySelector {
    @Autowired
    private RazorPayPaymentGateway razorPayPaymentGateway;
    @Autowired
    private StripePaymentGateway stripePaymentGateway;

    public void setRazorPayPaymentGateway(RazorPayPaymentGateway razorPayPaymentGateway,
            StripePaymentGateway stripePaymentGateway) {
        this.razorPayPaymentGateway = razorPayPaymentGateway;
        this.stripePaymentGateway = stripePaymentGateway;
    }

    public PaymentGateway getPaymentGateway(String paymentMethod) {
        if (paymentMethod == null) {
            return stripePaymentGateway; // Default to Stripe
        }

        switch (paymentMethod.toLowerCase()) {
            case "razorpay":
            case "upi":
            case "netbanking":
                return razorPayPaymentGateway;
            case "stripe":
            case "credit_card":
            case "debit_card":
                return stripePaymentGateway;
            default:
                // Log warning and default to Stripe or throw exception
                // For now, default to Stripe as a fallback
                return stripePaymentGateway;
        }
    }
}
