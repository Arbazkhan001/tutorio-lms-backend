package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.enums.EnrollmentStatus;
import com.affycloud.TutorioBackend.enums.ItemType;
import com.affycloud.TutorioBackend.models.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    List<Enrollment> findAllByStudent(Student student);

    Page<Enrollment> findByStudentId(UUID studentId, Pageable pageable);

    Page<Enrollment> findByStudentIdAndItemType(UUID studentId, ItemType itemType, Pageable pageable);

    Optional<Enrollment> findByStudentIdAndEbook_Id(UUID studentId, UUID ebookId);

    Optional<Enrollment> findByStudentIdAndTestSeries_Id(UUID studentId, UUID testSeriesId);

    Optional<Enrollment> findByStudentIdAndCourseAttempt_Id(UUID studentId, UUID courseAttemptId);

    long countByCourseBatch_Id(UUID courseBatchId);

    Page<Enrollment> findByCourseBatch_Id(UUID courseBatchId, Pageable pageable);

    Optional<Enrollment> findByStudent_Id(UUID studentId);

    Optional<Enrollment> findByStudentIdAndCourseAttemptId(UUID studentId, UUID courseAttemptId);

    List<Enrollment> findByStudentId(UUID studentId);

    boolean existsByStudentIdAndCourseAttemptId(UUID studentId, UUID courseAttemptId);

    /** Re-purchase check — only blocks if an ACTIVE enrollment already exists for this plan. */
    boolean existsByStudentIdAndCourseAttemptIdAndStatus(UUID studentId, UUID courseAttemptId, EnrollmentStatus status);

    void deleteByCourseAttempt(CourseAttempt courseAttempt);

    boolean existsByEbook(Ebook ebook);

    boolean existsByCourseAttempt(CourseAttempt courseAttempt);

    boolean existsByTestSeries(TestSeries testSeries);

    @Query("SELECT e.testSeries.id FROM Enrollment e WHERE e.student.id = :studentId AND e.testSeries IS NOT NULL")
    Set<UUID> findTestSeriesIdsByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT e.ebook.id FROM Enrollment e WHERE e.student.id = :studentId AND e.ebook IS NOT NULL")
    Set<UUID> findEbookIdsByStudentId(@Param("studentId") UUID studentId);

    /** Returns course IDs for ACTIVE enrollments only — expired enrollments are excluded so inMyLearning shows false and the student can re-purchase. */
    @Query("SELECT e.courseAttempt.courseType.course.id FROM Enrollment e WHERE e.student.id = :studentId AND e.courseAttempt IS NOT NULL AND e.status = com.affycloud.TutorioBackend.enums.EnrollmentStatus.ACTIVE")
    Set<UUID> findCourseIdsByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.tenantId = :tenantId AND e.enrolledAt BETWEEN :start AND :end")
    long countByStudentTenantIdAndEnrolledAtBetween(
            @Param("tenantId") UUID tenantId,
            @Param("start") Date start,
            @Param("end") Date end);

    // ─── Course analytics ──────────────────────────────────────────────────────

    /** Distinct enrolled students grouped by course type for a tenant. Returns [courseTypeId, studentCount]. */
    @Query("SELECT e.courseAttempt.courseType.id, COUNT(DISTINCT e.student.id) FROM Enrollment e " +
           "WHERE e.courseAttempt IS NOT NULL AND e.courseAttempt.courseType.course.tenantId = :tenantId " +
           "GROUP BY e.courseAttempt.courseType.id")
    List<Object[]> countEnrolledStudentsPerCourseType(@Param("tenantId") UUID tenantId);

    /** Distinct students enrolled in a single course type (for the drill-down roster). */
    @Query("SELECT DISTINCT e.student FROM Enrollment e " +
           "WHERE e.courseAttempt IS NOT NULL AND e.courseAttempt.courseType.id = :courseTypeId")
    List<Student> findDistinctStudentsByCourseTypeId(@Param("courseTypeId") UUID courseTypeId);

    /** Earliest enrollment date per student for a course type. Returns [studentId, enrolledAt]. */
    @Query("SELECT e.student.id, MIN(e.enrolledAt) FROM Enrollment e " +
           "WHERE e.courseAttempt IS NOT NULL AND e.courseAttempt.courseType.id = :courseTypeId " +
           "GROUP BY e.student.id")
    List<Object[]> findEnrolledAtPerStudentForCourseType(@Param("courseTypeId") UUID courseTypeId);

    /**
     * All enrollment rows for a course type with expiration info.
     * Returns [studentId, enrolledAt, expirationDays, batchName] ordered newest-first so the
     * caller can take the first entry per student as the active enrollment.
     */
    @Query("SELECT e.student.id, e.enrolledAt, e.courseAttempt.expirationDays, e.courseBatch.batch FROM Enrollment e " +
           "WHERE e.courseAttempt IS NOT NULL AND e.courseAttempt.courseType.id = :courseTypeId " +
           "ORDER BY e.enrolledAt DESC")
    List<Object[]> findEnrollmentDetailsForCourseType(@Param("courseTypeId") UUID courseTypeId);

    /** All ACTIVE COURSE enrollments for a specific student, eagerly fetching the course attempt and course type. */
    @Query("SELECT e FROM Enrollment e " +
           "JOIN FETCH e.courseAttempt ca " +
           "JOIN FETCH ca.courseType ct " +
           "JOIN FETCH ct.course " +
           "WHERE e.student.id = :studentId " +
           "AND e.itemType = com.affycloud.TutorioBackend.enums.ItemType.COURSE " +
           "AND e.status = com.affycloud.TutorioBackend.enums.EnrollmentStatus.ACTIVE")
    List<Enrollment> findCourseEnrollmentsByStudentId(@Param("studentId") UUID studentId);

    /** Content access guard — finds an ACTIVE enrollment for a student in a specific course type. */
    @Query("SELECT e FROM Enrollment e " +
           "WHERE e.student.id = :studentId " +
           "AND e.courseAttempt.courseType.id = :courseTypeId " +
           "AND e.status = com.affycloud.TutorioBackend.enums.EnrollmentStatus.ACTIVE")
    Optional<Enrollment> findActiveEnrollmentByStudentAndCourseType(
            @Param("studentId") UUID studentId,
            @Param("courseTypeId") UUID courseTypeId);

    /**
     * Returns ACTIVE course enrollments whose enrollmentEndAt is still NULL (pre-migration rows).
     * The scheduler backfills these before running the expiry query.
     */
    @Query("SELECT e FROM Enrollment e " +
           "JOIN FETCH e.courseAttempt ca " +
           "WHERE e.status = com.affycloud.TutorioBackend.enums.EnrollmentStatus.ACTIVE " +
           "AND e.itemType = com.affycloud.TutorioBackend.enums.ItemType.COURSE " +
           "AND e.enrollmentEndAt IS NULL " +
           "AND ca.expirationDays IS NOT NULL")
    List<Enrollment> findActiveCourseEnrollmentsWithNullEndAt();

    /**
     * Nightly expiry job — bulk-marks ACTIVE enrollments as EXPIRED when enrollmentEndAt has passed.
     * Uses the stored enrollmentEndAt (set at purchase time) so changing plan days never retroactively
     * affects existing students.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Enrollment e SET e.status = com.affycloud.TutorioBackend.enums.EnrollmentStatus.EXPIRED " +
           "WHERE e.status = com.affycloud.TutorioBackend.enums.EnrollmentStatus.ACTIVE " +
           "AND e.enrollmentEndAt IS NOT NULL " +
           "AND e.enrollmentEndAt < :now")
    int markExpiredEnrollments(@Param("now") Date now);

    /**
     * Certificate generation candidates — ACTIVE or already-EXPIRED enrollments without a certificate.
     * EXPIRED is included so enrollments that crossed enrollmentEndAt before the scheduler caught them
     * (e.g. the nightly expiry job ran first) still get a certificate instead of being skipped forever.
     */
    @Query("SELECT e FROM Enrollment e WHERE e.itemType = com.affycloud.TutorioBackend.enums.ItemType.COURSE " +
           "AND e.status IN (com.affycloud.TutorioBackend.enums.EnrollmentStatus.ACTIVE, " +
           "com.affycloud.TutorioBackend.enums.EnrollmentStatus.EXPIRED) " +
           "AND e.courseAttempt IS NOT NULL AND e.enrollmentEndAt IS NOT NULL " +
           "AND NOT EXISTS (SELECT 1 FROM IssuedCertificate ic " +
           "WHERE ic.enrollment.student.id = e.student.id " +
           "AND ic.enrollment.courseAttempt.courseType.id = e.courseAttempt.courseType.id)")
    List<Enrollment> findCandidatesForCertificateGeneration();

}
