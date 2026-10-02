package com.affycloud.TutorioBackend.services;

import java.io.UnsupportedEncodingException;

public interface SmsService {
    String sendOtp(String mobileNumber, String otp) throws UnsupportedEncodingException;
}
