package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.enrollment.EnrollmentAddDto;

import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.affycloud.TutorioBackend.dtos.student.StudentDetailDto;

public interface EnrollmentService {
    Map<String, Object> placeOrder(EnrollmentAddDto request);

    Map<String, Object> blockEnrollment(UUID enrollmentId);

    Map<String, Object> removeEnrollment(UUID studentId, UUID itemId, String itemType);

    Page<StudentDetailDto> getStudentsByBatchId(UUID batchId, Pageable pageable);
}