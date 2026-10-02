package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.handler.InvalidOperationException;
import com.affycloud.TutorioBackend.models.*;
import com.affycloud.TutorioBackend.services.RazorpayService;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RazorpayServiceImplementation implements RazorpayService {

    private final OkHttpClient client = new OkHttpClient();

    public Map<String, Object> createRazorpayOrder(Order order, User user) throws RazorpayException {
        RazorpayClient razorpayClient = new RazorpayClient(user.getRazorpayKey(), user.getRazorpaySecret());

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (int) (order.getTotalAmount() * 100)); // amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", /* "order_rcptid_" + */ order.getId());

        com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getId());
        response.put("razorpayOrderId", razorpayOrder.get("id"));
        response.put("amount", order.getTotalAmount() * 100); // in paise
        response.put("currency", "INR");
        response.put("key", user.getRazorpaySecret()); // Send to frontend for SDK usage

        return response;
    }

    public String generatePaymentLink(Order order, User user) throws RazorpayException {
        RazorpayClient razorpayClient = new RazorpayClient(user.getRazorpayKey(), user.getRazorpaySecret());

        JSONObject paymentLinkRequest = new JSONObject();
        paymentLinkRequest.put("amount", order.getTotalAmount() * 100); // Amount in paise
        paymentLinkRequest.put("currency", "INR");
        paymentLinkRequest.put("accept_partial", false);
        paymentLinkRequest.put("reference_id", order.getId().toString());
        paymentLinkRequest.put("description", "Payment for order #" + order.getId());

        JSONObject customer = new JSONObject();
        customer.put("name", order.getStudent().getName());
        customer.put("email", order.getStudent().getEmail());
        customer.put("contact", order.getStudent().getPhoneNumber());
        paymentLinkRequest.put("customer", customer);

        JSONObject notify = new JSONObject();
        notify.put("sms", true);
        notify.put("email", true);
        paymentLinkRequest.put("notify", notify);

        // paymentLinkRequest.put("callback_url",
        // "http://meta-code.localhost:5173/#/payment-status?orderId=" + order.getId());
        paymentLinkRequest.put("callback_url",
                "https://accent-talk.tutorio.in/#/payment-status?orderId=" + order.getId());
        paymentLinkRequest.put("callback_method", "get");

        PaymentLink paymentLink = razorpayClient.paymentLink.create(paymentLinkRequest);
        return paymentLink.get("short_url");
    }

    public boolean verifyPayment(String paymentId, User user) throws Exception {
        String url = "https://api.razorpay.com/v1/payments/" + paymentId;
        // String url = "https://api.razorpay.com/v1/orders/" + paymentId;
        String credentials = Credentials.basic(user.getRazorpayKey(), user.getRazorpaySecret());
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", credentials)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new Exception("Failed to verify payment: " + response.body().string());
        }

        JSONObject jsonResponse = new JSONObject(response.body().string());
        String status = jsonResponse.getString("status");

        return "captured".equals(status) || "paid".equals(status);
    }

    public void validateRazorpayCredentials(
            String key,
            String secret) {

        try {

            RazorpayClient razorpayClient = new RazorpayClient(key, secret);

            // Make minimal authenticated call
            razorpayClient.orders.fetchAll();

        } catch (RazorpayException e) {

            throw new InvalidOperationException(
                    "Invalid Razorpay credentials. Please check key and secret.");

        } catch (Exception e) {

            throw new InvalidOperationException(
                    "Unable to verify Razorpay credentials.");
        }
    }

}
