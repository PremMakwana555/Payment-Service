package com.ecommerce.payment_service.paymentGateway;

import com.razorpay.PaymentLink;
import org.json.JSONObject;
import com.razorpay.RazorpayClient;
import org.springframework.stereotype.Service;

@Service
public class RazorPayPaymentGateway implements PaymentGateway {

    @org.springframework.beans.factory.annotation.Value("${razorpay.api.key}")
    private String razorpayApiKey;

    @org.springframework.beans.factory.annotation.Value("${razorpay.api.secret}")
    private String razorpayApiSecret;

    @Override
    public String generatePaymentUrl() {
        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayApiKey,
                    razorpayApiSecret);
            JSONObject paymentLinkRequest = new JSONObject();
            paymentLinkRequest.put("amount", 1000);
            paymentLinkRequest.put("currency", "INR");
            paymentLinkRequest.put("accept_partial", false);
            paymentLinkRequest.put("expire_by", 1709345863);
            paymentLinkRequest.put("reference_id", "TS1989");
            paymentLinkRequest.put("description", "Payment for policy no #23456");

            JSONObject customer = new JSONObject();
            customer.put("contact", "+917337057594");
            customer.put("name", "Ankit Arora");
            customer.put("email", "arora.ankit7@gmail.com");
            paymentLinkRequest.put("customer", customer);

            JSONObject notify = new JSONObject();
            notify.put("sms", true);
            notify.put("email", true);
            paymentLinkRequest.put("notify", notify);

            paymentLinkRequest.put("callback_url", "https://google.com/");
            paymentLinkRequest.put("callback_method", "get");

            PaymentLink payment = razorpay.paymentLink.create(paymentLinkRequest);

            return payment.toString();
        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }
}