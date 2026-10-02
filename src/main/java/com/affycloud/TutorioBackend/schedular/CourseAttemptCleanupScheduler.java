package com.affycloud.TutorioBackend.schedular;

import com.affycloud.TutorioBackend.models.CourseAttempt;
import com.affycloud.TutorioBackend.models.Enrollment;
import com.affycloud.TutorioBackend.repositories.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Nightly job that marks ACTIVE enrollments as EXPIRED when their stored
 * {@code enrollmentEndAt} has passed. Also backfills {@code enrollmentEndAt}
 * for pre-migration rows where it is NULL, using enrolledAt + expirationDays.
 * Rows are never deleted — purchase history is always preserved.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CourseAttemptCleanupScheduler {

    private final EnrollmentRepository enrollmentRepository;

    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Kolkata")
    @Transactional
    public void expireEnrollments() {
        Date now = new Date();

        // ── Step 1: Backfill enrollmentEndAt for old rows where it is NULL ──────
        // Pre-migration enrollments don't have enrollment_end_at stored in DB.
        // Compute it from enrolledAt + expirationDays so the expiry query can pick them up.
        List<Enrollment> nullEndAt = enrollmentRepository.findActiveCourseEnrollmentsWithNullEndAt();

        int backfilled = 0;
        for (Enrollment e : nullEndAt) {
            CourseAttempt ca = e.getCourseAttempt();
            // expirationDays = null  → lifetime access, skip (leave enrollmentEndAt as NULL)
            // expirationDays = 0     → expired on enrollment date (common for misconfigured plans)
            // expirationDays > 0     → expires after N days
            if (ca != null && ca.getExpirationDays() != null) {
                long endMs = e.getEnrolledAt().getTime()
                        + ca.getExpirationDays() * 24L * 60L * 60L * 1000L;
                e.setEnrollmentEndAt(new Date(endMs));
                enrollmentRepository.save(e);
                backfilled++;
            }
        }

        if (backfilled > 0) {
            log.info("Enrollment expiry job: backfilled enrollmentEndAt for {} enrollment(s)", backfilled);
        }

        // ── Step 2: Mark as EXPIRED where enrollmentEndAt < now ─────────────────
        // Covers both newly-backfilled rows and rows already stored at purchase time.
        int count = enrollmentRepository.markExpiredEnrollments(now);
        log.info("Enrollment expiry job: {} enrollment(s) marked as EXPIRED", count);
    }
}
