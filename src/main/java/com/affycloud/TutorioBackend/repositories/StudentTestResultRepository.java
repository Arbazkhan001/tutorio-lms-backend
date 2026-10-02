package com.affycloud.TutorioBackend.repositories;


import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.models.StudentTestResult;
import com.affycloud.TutorioBackend.models.Test;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentTestResultRepository extends JpaRepository<StudentTestResult, UUID> {
    List<StudentTestResult> findByTest(Test test);
    Optional<StudentTestResult> findByTestAndStudent(Test test, Student student);
    List<StudentTestResult> findByStudentId(UUID studentId);
    Optional<StudentTestResult> findByTestIdAndStudentId(UUID testId, UUID studentId);
    Optional<StudentTestResult> findByStudentIdAndTestId(UUID studentId, UUID testId);



}
