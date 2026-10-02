package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.CourseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseTypeRepository extends JpaRepository<CourseType, UUID> {

    /**
     * Course-type metadata for a tenant, used as the base rows of the course analytics table.
     * Returns [courseTypeId, courseTypeLabel, courseId, courseTitle, category, courseIsActive].
     */
    @Query("SELECT ct.id, ct.courseType, ct.course.id, ct.course.title, ct.course.category, ct.course.isActive " +
           "FROM CourseType ct WHERE ct.course.tenantId = :tenantId")
    List<Object[]> findCourseTypeMetaByTenant(@Param("tenantId") UUID tenantId);

}
