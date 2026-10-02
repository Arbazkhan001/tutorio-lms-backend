package com.affycloud.TutorioBackend.services;



public interface EmailService {

    void sendOtp(String toEmail, String otp);
    void sendMail(String email, String message);
    void sendOrderConfirmationEmail(String to, String subject, String body);
}
