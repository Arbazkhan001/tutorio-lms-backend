package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.models.Video;
import com.affycloud.TutorioBackend.models.VideoProgress;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoProgressRepository extends JpaRepository<VideoProgress, UUID> {
    Optional<VideoProgress> findFirstByStudentAndVideo(Student student, Video video);

    @Query("""
    SELECT vp FROM VideoProgress vp
    WHERE vp.student.id = :studentId 
    AND vp.updatedAt = (
        SELECT MAX(vp2.updatedAt) FROM VideoProgress vp2 
        WHERE vp2.student.id = :studentId 
        AND vp2.video.chapter.id = vp.video.chapter.id
    )
    ORDER BY vp.updatedAt DESC
    """)
    List<VideoProgress> findLatestVideosPerChapter(@Param("studentId") UUID studentId, Pageable pageable);

    List<VideoProgress> findByVideoId(UUID videoId);

    // ─── Course analytics ──────────────────────────────────────────────────────

    /**
     * Watch aggregates grouped by course type for a tenant.
     * Returns [courseTypeId, startedVideoRows, distinctLearners, totalWatchSeconds].
     * Each VideoProgress row is unique per (student, video), so startedVideoRows is also
     * the total of "videos started" across all learners in that course type.
     */
    @Query("SELECT vp.video.chapter.subject.courseType.id, COUNT(vp), " +
           "COUNT(DISTINCT vp.student.id), COALESCE(SUM(vp.watchTime), 0) " +
           "FROM VideoProgress vp " +
           "WHERE vp.video.chapter.subject.courseType.course.tenantId = :tenantId " +
           "GROUP BY vp.video.chapter.subject.courseType.id")
    List<Object[]> aggregateProgressPerCourseType(@Param("tenantId") UUID tenantId);

    /**
     * Per-student watch aggregates within a single course type.
     * Returns [studentId, videosStarted, videosCompleted, totalWatchSeconds, lastActiveAt].
     * A video counts as "completed" when its watch time meets the threshold (seconds).
     */
    @Query("SELECT vp.student.id, COUNT(vp), " +
           "SUM(CASE WHEN vp.watchTime >= :completeThreshold THEN 1 ELSE 0 END), " +
           "COALESCE(SUM(vp.watchTime), 0), MAX(vp.updatedAt) " +
           "FROM VideoProgress vp " +
           "WHERE vp.video.chapter.subject.courseType.id = :courseTypeId " +
           "GROUP BY vp.student.id")
    List<Object[]> aggregateProgressPerStudentForCourseType(
            @Param("courseTypeId") UUID courseTypeId,
            @Param("completeThreshold") double completeThreshold);

    /**
     * Per-course-type progress for a single student across multiple course types.
     * Returns [courseTypeId, videosStarted, videosCompleted, totalWatchSeconds, lastActiveAt].
     */
    @Query("SELECT vp.video.chapter.subject.courseType.id, COUNT(vp), " +
           "SUM(CASE WHEN vp.watchTime >= :completeThreshold THEN 1 ELSE 0 END), " +
           "COALESCE(SUM(vp.watchTime), 0), MAX(vp.updatedAt) " +
           "FROM VideoProgress vp " +
           "WHERE vp.student.id = :studentId " +
           "AND vp.video.chapter.subject.courseType.id IN :courseTypeIds " +
           "GROUP BY vp.video.chapter.subject.courseType.id")
    List<Object[]> aggregateProgressForStudentPerCourseTypes(
            @Param("studentId") UUID studentId,
            @Param("courseTypeIds") Collection<UUID> courseTypeIds,
            @Param("completeThreshold") double completeThreshold);
}
