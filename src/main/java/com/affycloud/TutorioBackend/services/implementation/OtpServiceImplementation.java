package com.affycloud.TutorioBackend.services.implementation;


import com.affycloud.TutorioBackend.services.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpServiceImplementation implements OtpService {


    private final CacheManager cacheManager;

    private static final int RESEND_INTERVAL_SECONDS = 60; // 1 minute rate limit

    @Autowired
    public OtpServiceImplementation(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // Generates a 6-digit OTP
    @Override
    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(1000000));
    }



    @Override
    public void storeOtp(String phoneNumber, String otp) {
        cacheManager.getCache("otpCache").put(phoneNumber, otp);
        cacheManager.getCache("otpRequestTimeCache").put(phoneNumber, LocalDateTime.now());
    }

    // Retrieves the OTP from the cache
    @Override
    public String getOtp(String email) {
        return cacheManager.getCache("otpCache").get(email, String.class);
    }

    // Invalidates the OTP in the cache
    @Override
    public void invalidateOtp(String email) {
        cacheManager.getCache("otpCache").evict(email);
    }

    public boolean canResendOtp(String phoneNumber) {
        LocalDateTime lastRequestTime = cacheManager.getCache("otpRequestTimeCache")
                .get(phoneNumber, LocalDateTime.class);
        if (lastRequestTime == null) {
            return true;
        }
        return Duration.between(lastRequestTime, LocalDateTime.now()).getSeconds() >= RESEND_INTERVAL_SECONDS;
    }
}

