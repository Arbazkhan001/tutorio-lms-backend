package com.affycloud.TutorioBackend.repositories;


import com.affycloud.TutorioBackend.models.Subject;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, UUID> {
    List<Subject> findByCourseTypeId(UUID courseTypeId, Sort sort);

    List<Subject> findByCourseTypeIdAndIsActiveTrue(UUID courseTypeId, Sort sort);
}
