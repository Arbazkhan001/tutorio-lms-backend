package com.affycloud.TutorioBackend.repositories;
import com.affycloud.TutorioBackend.models.CourseAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseAttemptRepository extends JpaRepository<CourseAttempt, UUID> {
    @Query("SELECT c FROM CourseAttempt c WHERE c.attempt <= CURRENT_TIMESTAMP")
    List<CourseAttempt> findExpiredAttempts();
}
