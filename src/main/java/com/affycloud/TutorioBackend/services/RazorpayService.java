package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.models.Order;
import com.affycloud.TutorioBackend.models.User;
import com.razorpay.RazorpayException;

import java.util.Map;

public interface RazorpayService {
    Map<String, Object> createRazorpayOrder(Order order, User user) throws RazorpayException;
    String generatePaymentLink(Order order, User user) throws RazorpayException;
    boolean verifyPayment(String paymentId, User user) throws Exception;
    void validateRazorpayCredentials(
            String key,
            String secret);

}
