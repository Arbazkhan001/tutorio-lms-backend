package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.Video;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
public interface VideoRepository extends JpaRepository<Video, UUID> {
    List<Video> findByCourseTypeId(UUID courseTypeId,Sort sort);
    List<Video>  findByChapterId(UUID chapterId, Sort sort);

    /** Total videos in a single course type (via the chapter → subject → courseType path). */
    @Query("SELECT COUNT(v) FROM Video v WHERE v.chapter.subject.courseType.id = :courseTypeId")
    long countByCourseTypeId(@Param("courseTypeId") UUID courseTypeId);

    /** Video counts grouped by course type for a tenant. Returns rows of [courseTypeId, count]. */
    @Query("SELECT v.chapter.subject.courseType.id, COUNT(v) FROM Video v " +
           "WHERE v.chapter.subject.courseType.course.tenantId = :tenantId " +
           "GROUP BY v.chapter.subject.courseType.id")
    List<Object[]> countVideosPerCourseType(@Param("tenantId") UUID tenantId);

    /** Video counts grouped by course type for a specific set of course type IDs. Returns rows of [courseTypeId, count]. */
    @Query("SELECT v.chapter.subject.courseType.id, COUNT(v) FROM Video v " +
           "WHERE v.chapter.subject.courseType.id IN :courseTypeIds " +
           "GROUP BY v.chapter.subject.courseType.id")
    List<Object[]> countVideosPerCourseTypeIds(@Param("courseTypeIds") Collection<UUID> courseTypeIds);

}
