package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.enrollment.EnrollmentAddDto;
import com.affycloud.TutorioBackend.dtos.enrollment.EnrollmentDTO;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.enums.ItemType;
import com.affycloud.TutorioBackend.services.EnrollmentService;
import com.affycloud.TutorioBackend.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentService service;

    @Autowired
    private OrderService orderService;

    @Operation(summary = "Enroll a student in a course or test series or ebook it also have payment gateway")
    @PostMapping("/enroll")
    public ResponseEntity<Map<String, Object>> placeOrder(@RequestBody EnrollmentAddDto request) {
        Map<String, Object> response = service.placeOrder(request);
        if (response.containsKey("error") && (boolean) response.get("error")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Get My Learning by Student ID")
    @GetMapping("/my-learning/{studentId}")
    public ResponseEntity<ApiResponseWithMeta> getMyLearning(
            @PathVariable UUID studentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter) {

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("enrolledAt").descending());
        Page<EnrollmentDTO> enrollmentPage;

        if (filter != null && !filter.isBlank()) {
            ItemType itemType = null;
            switch (filter.toLowerCase()) {
                case "test":
                    itemType = ItemType.TESTSERIES;
                    break;
                case "ebook":
                    itemType = ItemType.EBOOK;
                    break;
                case "course":
                    itemType = ItemType.COURSE;
                    break;
                default:
                    // If the filter is unrecognized, we simply ignore it and return all records.
                    break;
            }
            if (itemType != null) {
                enrollmentPage = orderService.getEnrolledItemsByType(studentId, itemType, pageable);
            } else {
                enrollmentPage = orderService.getEnrolledItems(studentId, pageable);
            }
        } else {
            enrollmentPage = orderService.getEnrolledItems(studentId, pageable);
        }

        Map<String, Object> meta = Map.of(
                "totalEnrolled", enrollmentPage.getTotalElements(),
                "totalPages", enrollmentPage.getTotalPages(),
                "currentPage", enrollmentPage.getNumber() + 1,
                "pageSize", enrollmentPage.getSize(),
                "studentId", studentId);

        ApiResponseWithMeta response = new ApiResponseWithMeta(
                enrollmentPage.getContent().isEmpty() ? "No enrolled items found."
                        : "Enrolled items retrieved successfully.",
                false,
                Map.of("enrolledItems", enrollmentPage.getContent(), "meta", meta));

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Block Course for a student because of some reason")
    @PutMapping("/block/{enrollmentId}")
    public ResponseEntity<?> toggleIsActive(@PathVariable UUID enrollmentId) {
        Map<String, Object> response = service.blockEnrollment(enrollmentId);
        if ((boolean) response.getOrDefault("error", true)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove an Enrollment by studentId, itemId and itemType")
    @DeleteMapping("/unenroll")
    public ResponseEntity<Map<String, Object>> removeEnrollment(@RequestParam UUID studentId,
            @RequestParam UUID itemId,
            @RequestParam String itemType) {
        Map<String, Object> response = service.removeEnrollment(studentId, itemId, itemType);
        if ((boolean) response.getOrDefault("error", true)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get students enrolled in a specific batch")
    @GetMapping("/batch/{batchId}/students")
    public ResponseEntity<ApiResponseWithMeta> getStudentsByBatchId(
            @PathVariable UUID batchId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<com.affycloud.TutorioBackend.dtos.student.StudentDetailDto> studentPage = service
                .getStudentsByBatchId(batchId, pageable);

        Map<String, Object> meta = Map.of(
                "totalStudents", studentPage.getTotalElements(),
                "totalPages", studentPage.getTotalPages(),
                "currentPage", studentPage.getNumber() ,
                "pageSize", studentPage.getSize(),
                "batchId", batchId);

        ApiResponseWithMeta response = new ApiResponseWithMeta(
                studentPage.getContent().isEmpty() ? "No students found for this batch."
                        : "Students retrieved successfully.",
                false,
                Map.of("students", studentPage.getContent(), "meta", meta));

        return ResponseEntity.ok(response);
    }
}