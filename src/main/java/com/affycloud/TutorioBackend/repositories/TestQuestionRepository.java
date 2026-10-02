package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TestQuestionRepository extends JpaRepository<Question, UUID> {
}
