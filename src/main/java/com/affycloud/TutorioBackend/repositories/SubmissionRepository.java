package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
    List<Submission> findByExamId(UUID examId);
    List<Submission> findByStudentIdAndExamId(UUID studentId, UUID examId);
    boolean existsByStudentIdAndExamId(UUID studentId, UUID examId);
    List<Submission> findByStudentId(UUID studentId);



}
