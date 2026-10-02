package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.CourseAttempt;
import com.affycloud.TutorioBackend.models.CourseBundle;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseBundleRepository extends JpaRepository<CourseBundle, UUID> {

    boolean existsByCourseAttempts(CourseAttempt courseAttempt);
    List<CourseBundle> findAllByTenantId(UUID tenantId, Sort sort);
    long countByTenantId(UUID tenantId);

}
