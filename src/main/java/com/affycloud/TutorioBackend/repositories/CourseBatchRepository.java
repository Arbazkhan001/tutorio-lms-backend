package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.*;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseBatchRepository extends JpaRepository<CourseBatch, UUID> {

    List<CourseBatch> findAllByCourseAttempt(CourseAttempt courseAttempt, Sort sort);

}
