package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.models.CourseAttempt;
import com.affycloud.TutorioBackend.models.CourseType;
import com.affycloud.TutorioBackend.models.Enrollment;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.repositories.CourseTypeRepository;
import com.affycloud.TutorioBackend.repositories.EnrollmentRepository;
import com.affycloud.TutorioBackend.repositories.VideoProgressRepository;
import com.affycloud.TutorioBackend.repositories.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Read-only course-progress analytics, aggregated from {@code VideoProgress}.
 *
 * <p>Completion model (approximation — no per-video duration exists in the schema yet):
 * a video is <b>started</b> when a {@code VideoProgress} row exists for (student, video),
 * and <b>completed</b> when its {@code watchTime} reaches {@link #COMPLETE_THRESHOLD_SECONDS}.
 * A student's course progress % = videos started ÷ total videos in the course type.
 */
@Service
@RequiredArgsConstructor
public class CourseAnalyticsService {

    private final CourseTypeRepository courseTypeRepository;
    private final VideoRepository videoRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final VideoProgressRepository videoProgressRepository;

    /** Watch-time (seconds) at which a video is treated as completed. */
    private static final double COMPLETE_THRESHOLD_SECONDS = 60.0;

    // ─── Overview: one row per course type ─────────────────────────────────────

    public Map<String, Object> getCourseAnalyticsOverview(UUID tenantId) {
        List<Object[]> meta = courseTypeRepository.findCourseTypeMetaByTenant(tenantId);

        Map<UUID, Long> videosByCt   = toLongMap(videoRepository.countVideosPerCourseType(tenantId));
        Map<UUID, Long> enrolledByCt = toLongMap(enrollmentRepository.countEnrolledStudentsPerCourseType(tenantId));

        // courseTypeId -> [startedRows, distinctLearners, watchSeconds]
        Map<UUID, double[]> progressByCt = new HashMap<>();
        for (Object[] r : videoProgressRepository.aggregateProgressPerCourseType(tenantId)) {
            progressByCt.put((UUID) r[0], new double[]{
                    num(r[1]).doubleValue(), num(r[2]).doubleValue(), num(r[3]).doubleValue()
            });
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        double sumWatchHours = 0, sumCompletion = 0;
        long totalEnrollments = 0, totalActiveLearners = 0;
        int countedForAvg = 0;

        for (Object[] m : meta) {
            UUID courseTypeId = (UUID) m[0];

            long totalVideos    = videosByCt.getOrDefault(courseTypeId, 0L);
            long enrolled       = enrolledByCt.getOrDefault(courseTypeId, 0L);
            double[] p          = progressByCt.getOrDefault(courseTypeId, new double[]{0, 0, 0});
            long startedRows    = (long) p[0];
            long activeLearners = (long) p[1];
            double watchSeconds = p[2];

            double avgProgress = (enrolled > 0 && totalVideos > 0)
                    ? Math.min(100.0, (startedRows / (double) (enrolled * totalVideos)) * 100.0)
                    : 0.0;
            double engagementRate = enrolled > 0
                    ? Math.min(100.0, (activeLearners / (double) enrolled) * 100.0)
                    : 0.0;
            double watchHours = watchSeconds / 3600.0;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("courseTypeId", courseTypeId);
            row.put("courseTypeLabel", m[1]);
            row.put("courseId", m[2]);
            row.put("courseTitle", m[3]);
            row.put("category", m[4]);
            row.put("isActive", m[5]);
            row.put("totalVideos", totalVideos);
            row.put("enrolledStudents", enrolled);
            row.put("activeLearners", activeLearners);
            row.put("videosStarted", startedRows);
            row.put("avgProgressPercent", round1(avgProgress));
            row.put("engagementRate", round1(engagementRate));
            row.put("watchHours", round1(watchHours));
            rows.add(row);

            sumWatchHours += watchHours;
            totalEnrollments += enrolled;
            totalActiveLearners += activeLearners;
            if (enrolled > 0) { sumCompletion += avgProgress; countedForAvg++; }
        }

        // Most-engaged course types first.
        rows.sort(Comparator
                .comparingLong((Map<String, Object> r) -> (Long) r.get("enrolledStudents")).reversed()
                .thenComparing(r -> (Double) r.get("watchHours"), Comparator.reverseOrder()));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCourseTypes", meta.size());
        summary.put("totalEnrollments", totalEnrollments);
        summary.put("totalActiveLearners", totalActiveLearners);
        summary.put("totalWatchHours", round1(sumWatchHours));
        summary.put("avgCompletionPercent", round1(countedForAvg > 0 ? sumCompletion / countedForAvg : 0));

        Map<String, Object> meta2 = new LinkedHashMap<>();
        meta2.put("summary", summary);
        meta2.put("courses", rows);
        return meta2;
    }

    // ─── Drill-down: students within one course type ───────────────────────────

    public Map<String, Object> getCourseTypeDrilldown(UUID tenantId, UUID courseTypeId) {
        CourseType ct = courseTypeRepository.findById(courseTypeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course type not found"));

        // Tenant scoping — never reveal another organisation's data.
        if (ct.getCourse() == null || !tenantId.equals(ct.getCourse().getTenantId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course type not found");
        }

        long totalVideos = videoRepository.countByCourseTypeId(courseTypeId);
        List<Student> students = enrollmentRepository.findDistinctStudentsByCourseTypeId(courseTypeId);

        // studentId -> [started, completed, watchSeconds, lastActive]
        Map<UUID, Object[]> progress = new HashMap<>();
        for (Object[] r : videoProgressRepository
                .aggregateProgressPerStudentForCourseType(courseTypeId, COMPLETE_THRESHOLD_SECONDS)) {
            progress.put((UUID) r[0], r);
        }

        // studentId -> [enrolledAt, expirationDays]  (latest enrollment wins — list is DESC by enrolledAt)
        Map<UUID, Object[]> enrollmentInfo = new HashMap<>();
        for (Object[] r : enrollmentRepository.findEnrollmentDetailsForCourseType(courseTypeId)) {
            enrollmentInfo.putIfAbsent((UUID) r[0], r); // first entry = most recent
        }

        List<Map<String, Object>> studentRows = new ArrayList<>();
        int notStarted = 0, inProgress = 0, completed = 0;
        double sumProgress = 0;

        for (Student s : students) {
            Object[] p = progress.get(s.getId());
            long started      = p != null ? num(p[1]).longValue() : 0;
            long completedVid = p != null ? num(p[2]).longValue() : 0;
            double watchSec   = p != null ? num(p[3]).doubleValue() : 0;
            Date lastActive   = p != null ? (Date) p[4] : null;

            double progressPercent = totalVideos > 0
                    ? Math.min(100.0, (started / (double) totalVideos) * 100.0)
                    : 0.0;

            String status;
            if (started == 0)                              { status = "NOT_STARTED"; notStarted++; }
            else if (totalVideos > 0 && started >= totalVideos) { status = "COMPLETED"; completed++; }
            else                                           { status = "IN_PROGRESS"; inProgress++; }
            sumProgress += progressPercent;

            // Compute enrolledAt and enrollmentEndAt from the latest enrollment
            Object[] ei = enrollmentInfo.get(s.getId());
            Date enAt = ei != null ? (Date) ei[1] : null;
            String enrollmentEndAt = null;
            if (enAt != null && ei[2] instanceof Number) {
                int expDays = ((Number) ei[2]).intValue();
                long endMs = enAt.getTime() + expDays * 24L * 60L * 60L * 1000L;
                enrollmentEndAt = new java.sql.Timestamp(endMs).toString();
            }
            String batchName = ei != null && ei.length > 3 ? (String) ei[3] : null;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("studentId", s.getId());
            row.put("studentName", s.getName());
            row.put("email", s.getEmail());
            row.put("phoneNumber", s.getPhoneNumber());
            row.put("batchName", batchName);
            row.put("videosStarted", started);
            row.put("videosCompleted", completedVid);
            row.put("totalVideos", totalVideos);
            row.put("progressPercent", round1(progressPercent));
            row.put("watchMinutes", round1(watchSec / 60.0));
            row.put("lastActiveAt", lastActive);
            row.put("enrolledAt", enAt);
            row.put("enrollmentEndAt", enrollmentEndAt);
            row.put("status", status);
            studentRows.add(row);
        }

        // Highest progress first, then most recently active.
        studentRows.sort(Comparator
                .comparingDouble((Map<String, Object> r) -> (Double) r.get("progressPercent")).reversed()
                .thenComparing(r -> {
                    Date d = (Date) r.get("lastActiveAt");
                    return d != null ? d.getTime() : 0L;
                }, Comparator.reverseOrder()));

        Map<String, Object> course = new LinkedHashMap<>();
        course.put("courseTypeId", ct.getId());
        course.put("courseTypeLabel", ct.getCourseType());
        course.put("courseId", ct.getCourse().getId());
        course.put("courseTitle", ct.getCourse().getTitle());
        course.put("category", ct.getCourse().getCategory());
        course.put("totalVideos", totalVideos);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("enrolledStudents", students.size());
        summary.put("notStarted", notStarted);
        summary.put("inProgress", inProgress);
        summary.put("completed", completed);
        summary.put("avgProgressPercent", round1(students.isEmpty() ? 0 : sumProgress / students.size()));

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("course", course);
        meta.put("summary", summary);
        meta.put("students", studentRows);
        return meta;
    }

    // ─── Student's own progress across all enrolled courses ───────────────────

    public Map<String, Object> getStudentProgressOverview(UUID studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findCourseEnrollmentsByStudentId(studentId);

        if (enrollments.isEmpty()) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("summary", Map.of(
                    "totalEnrolled", 0, "completed", 0,
                    "inProgress", 0, "notStarted", 0, "totalWatchHours", 0.0));
            result.put("courses", List.of());
            return result;
        }

        // Collect distinct courseTypeIds from the enrollments
        Set<UUID> courseTypeIds = new HashSet<>();
        for (Enrollment e : enrollments) {
            if (e.getCourseAttempt() != null && e.getCourseAttempt().getCourseType() != null) {
                courseTypeIds.add(e.getCourseAttempt().getCourseType().getId());
            }
        }

        // Video counts per course type
        Map<UUID, Long> videosByCt = toLongMap(videoRepository.countVideosPerCourseTypeIds(courseTypeIds));

        // Progress for this student per course type
        Map<UUID, Object[]> progressByCt = new HashMap<>();
        for (Object[] r : videoProgressRepository.aggregateProgressForStudentPerCourseTypes(
                studentId, courseTypeIds, COMPLETE_THRESHOLD_SECONDS)) {
            progressByCt.put((UUID) r[0], r);
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        int notStarted = 0, inProgress = 0, completed = 0;
        double totalWatchSeconds = 0;

        for (Enrollment e : enrollments) {
            if (e.getCourseAttempt() == null) continue;
            CourseAttempt ca = e.getCourseAttempt();
            CourseType ct = ca.getCourseType();
            UUID courseTypeId = ct.getId();

            long totalVideos = videosByCt.getOrDefault(courseTypeId, 0L);
            Object[] p = progressByCt.get(courseTypeId);
            long started      = p != null ? num(p[1]).longValue()    : 0;
            long completedVid = p != null ? num(p[2]).longValue()    : 0;
            double watchSec   = p != null ? num(p[3]).doubleValue()  : 0;
            Date lastActive   = p != null ? (Date) p[4]              : null;

            double progressPercent = totalVideos > 0
                    ? Math.min(100.0, (started / (double) totalVideos) * 100.0) : 0.0;

            String status;
            if (started == 0)                                    { status = "NOT_STARTED"; notStarted++; }
            else if (totalVideos > 0 && started >= totalVideos) { status = "COMPLETED";   completed++;  }
            else                                                  { status = "IN_PROGRESS"; inProgress++; }
            totalWatchSeconds += watchSec;

            // Compute enrollmentEndAt = enrolledAt + expirationDays
            String enrollmentEndAt = null;
            if (e.getEnrolledAt() != null && ca.getExpirationDays() != null) {
                long endMs = e.getEnrolledAt().getTime() + ca.getExpirationDays() * 24L * 60L * 60L * 1000L;
                enrollmentEndAt = new java.sql.Timestamp(endMs).toString();
            }

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("courseTitle",      ct.getCourse() != null ? ct.getCourse().getTitle()     : null);
            row.put("courseTypeLabel",  ct.getCourseType());
            row.put("thumbnail",        ct.getCourse() != null ? ct.getCourse().getThumbnail() : null);
            row.put("plan",             ca.getPlan());
            row.put("enrolledAt",       e.getEnrolledAt() != null ? e.getEnrolledAt().toString() : null);
            row.put("enrollmentEndAt",  enrollmentEndAt);
            row.put("progressPercent",  round1(progressPercent));
            row.put("videosStarted",    started);
            row.put("videosCompleted",  completedVid);
            row.put("totalVideos",      totalVideos);
            row.put("watchMinutes",     round1(watchSec / 60.0));
            row.put("lastActiveAt",     lastActive);
            row.put("status",           status);
            rows.add(row);
        }

        // Highest progress first
        rows.sort(Comparator
                .comparingDouble((Map<String, Object> r) -> (Double) r.get("progressPercent")).reversed());

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalEnrolled",   enrollments.size());
        summary.put("completed",        completed);
        summary.put("inProgress",       inProgress);
        summary.put("notStarted",       notStarted);
        summary.put("totalWatchHours",  round1(totalWatchSeconds / 3600.0));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("courses", rows);
        return result;
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private static Map<UUID, Long> toLongMap(List<Object[]> rows) {
        Map<UUID, Long> map = new HashMap<>();
        for (Object[] r : rows) {
            map.put((UUID) r[0], num(r[1]).longValue());
        }
        return map;
    }

    private static Number num(Object o) {
        return o instanceof Number ? (Number) o : 0;
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
