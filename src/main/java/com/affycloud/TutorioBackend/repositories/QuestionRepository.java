package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.Exam;
import com.affycloud.TutorioBackend.models.ExamQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<ExamQuestion, UUID> {
        List<ExamQuestion> findByExam(Exam exam);

}
