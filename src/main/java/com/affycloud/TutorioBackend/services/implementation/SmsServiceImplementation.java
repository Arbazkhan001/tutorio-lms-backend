package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.services.SmsService;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class SmsServiceImplementation implements  SmsService{
    private final OkHttpClient client = new OkHttpClient();
    private static final String AUTH_KEY = "2ce4ab9aa265f16e42797875b38a16e"; // Replace with actual Auth Key
    private static final String SENDER_ID = "EFRVST";
    private static final String ROUTE_ID = "1";
    private static final String CONTENT_TYPE = "english";

    @Override
    public String sendOtp(String mobileNumber, String otp) throws UnsupportedEncodingException {

        String customMessage = String.format(
                "Dear user, your Effervescent classes login otp %s. Please do not share the otp with anyone.",
                otp
        );

        String encodedMessage = URLEncoder.encode(customMessage, StandardCharsets.UTF_8.toString());

        String url = String.format(
                "http://msg.msgclub.net/rest/services/sendSMS/sendGroupSms?AUTH_KEY=%s&message=%s&senderId=%s&routeId=%s&mobileNos=%s&smsContentType=%s",
                AUTH_KEY,
                encodedMessage,
                SENDER_ID,
                ROUTE_ID,
                mobileNumber,
                CONTENT_TYPE
        );

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Cache-Control", "no-cache")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            return response.body() != null ? response.body().string() : "Empty response";
        } catch (Exception e) {

            return "Failed to send SMS: " + e.getMessage();
        }
    }
}
