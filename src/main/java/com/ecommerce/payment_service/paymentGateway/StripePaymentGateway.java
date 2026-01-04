package com.ecommerce.payment_service.paymentGateway;

import com.stripe.Stripe;
import com.stripe.model.PaymentLink;
import com.stripe.model.Price;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StripePaymentGateway implements PaymentGateway {
    @org.springframework.beans.factory.annotation.Value("${stripe.api.key}")
    private String stripeApiKey;

    @Override
    public String generatePaymentUrl() {

        Stripe.apiKey = stripeApiKey;

        try {
            Map<String, Object> productData = new HashMap<>();
            productData.put("name", "Test Product");

            Map<String, Object> productParam = new HashMap<>();
            productParam.put("unit_amount", 10000);
            productParam.put("currency", "inr");
            productParam.put("product_data", productData);

            // Create price
            Price price = Price.create(productParam);

            // Create price line item
            Map<String, Object> priceLineItem = new HashMap<>();
            priceLineItem.put("price", price.getId());
            priceLineItem.put("quantity", 1);

            List<Object> lineItems = new ArrayList<>();
            lineItems.add(priceLineItem);

            Map<String, Object> finalParams = new HashMap<>();
            finalParams.put("line_items", lineItems);

            Map<String, Object> afterCompletion = new HashMap<>();
            afterCompletion.put("type", "redirect");

            Map<String, Object> redirect = new HashMap<>();
            redirect.put("url", "https://google.com?payment_id={CHECKOUT_SESSION_ID}");

            afterCompletion.put("redirect", redirect);

            finalParams.put("after_completion", afterCompletion);

            PaymentLink paymentLink = PaymentLink.create(finalParams);

            return paymentLink.getUrl();
        } catch (Exception e) {
            System.out.println(e);
        }

        return null;
    }
}