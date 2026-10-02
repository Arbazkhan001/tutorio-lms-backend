package com.affycloud.TutorioBackend.schedular;

import com.affycloud.TutorioBackend.services.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CertificateGenerationScheduler {

    private final CertificateService certificateService;

    /** Runs daily at 02:02 IST (after enrollment expiry job) — auto-issues certificates for eligible course enrollments. */
    @Scheduled(cron = "0 2 2 * * ?", zone = "Asia/Kolkata")
    public void runCertificateGeneration() {
        log.info("Starting scheduled certificate generation run");
        certificateService.generateCertificatesForEligibleEnrollments();
        log.info("Finished scheduled certificate generation run");
    }
}
