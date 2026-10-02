package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

public interface TestRepository extends JpaRepository<Test, UUID> {
    List<Test> findByCourseType(UUID courseType, Sort sort);
}
