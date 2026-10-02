package com.affycloud.TutorioBackend.configurations;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCache otpCache = new CaffeineCache("otpCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(5, TimeUnit.MINUTES) // OTP expires in 5 minutes
                        .build());

        CaffeineCache otpRequestTimeCache = new CaffeineCache("otpRequestTimeCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(1, TimeUnit.MINUTES) // rate-limit resend (1 minute)
                        .build());

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(Arrays.asList(otpCache, otpRequestTimeCache));
        return manager;
    }
}

