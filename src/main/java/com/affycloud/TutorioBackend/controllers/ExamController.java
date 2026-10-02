//package com.affycloud.TutorioBackend.controllers;
//
//import com.affycloud.TutorioBackend.dtos.exam.*;
//import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
//import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
//import com.affycloud.TutorioBackend.services.ExamService;
//import io.swagger.v3.oas.annotations.Operation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//
//@RestController
//@RequestMapping("/api/exams")
//public class ExamController {
//
//    @Autowired
//    private ExamService examService;
//
//    @Operation(summary = "Create a new exam.",
//            description = "Creates a new exam.")
//    @PostMapping("/create")
//    public ResponseEntity<?> createExam(
//            @RequestBody ExamDTO examDTO) {
//        try {
//            ExamResponseDTO savedExam = examService.createExam(examDTO);
//            return ResponseEntity.ok(new ApiResponse("Exam created successfully!", false));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponseWithMeta("Error: " + e.getMessage(), true, null));
//        }
//    }
//
//    @Operation(summary = "Get all exams", description = "Retrieves a list of all exams with metadata.")
//    @GetMapping("/all")
//    public ResponseEntity<ApiResponseWithMeta> getAllExams() {
//        List<ExamResponseDTO> exams = examService.getAllExams();
//        Map<String, Object> meta = new HashMap<>();
//        meta.put("totalExams", exams.size());
//        meta.put("data", exams);
//        return ResponseEntity.ok(new ApiResponseWithMeta("Exams fetched successfully", false, meta));
//    }
//
//    @Operation(summary = "Get exam by ID", description = "Retrieves details of an exam using its unique ID.")
//    @GetMapping("/{id}")
//    public ResponseEntity<ApiResponseWithMeta> getExamById(@PathVariable UUID id) {
//        try {
//            ExamResponseDTO exam = examService.getExamById(id);
//            Map<String, Object> meta = new HashMap<>();
//            meta.put("exam", exam);
//            return ResponseEntity.ok(new ApiResponseWithMeta("Exam retrieved successfully", false, meta));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(new ApiResponseWithMeta(e.getMessage(), true, null));
//        }
//    }
//
//    @Operation(summary = "Update an exam", description = "Updates the details of an existing exam using its ID.")
//    @PutMapping("/{id}")
//    public ResponseEntity<ApiResponse> updateExam(@PathVariable UUID id, @RequestBody ExamDTO examDTO) {
//        boolean isUpdated = examService.updateExam(id, examDTO);
//
//        if (isUpdated) {
//            return ResponseEntity.ok(new ApiResponse("Exam updated successfully!", false));
//        } else {
//            return ResponseEntity.status(404).body(new ApiResponse("Exam not found!", true));
//        }
//    }
//
//    @Operation(summary = "Delete an exam", description = "Deletes an exam from the database using its ID.")
//    @DeleteMapping("/{id}")
//    public ResponseEntity<ApiResponse> deleteExam(@PathVariable UUID id) {
//        boolean isDeleted = examService.deleteExam(id);
//
//        if (isDeleted) {
//            return ResponseEntity.ok(new ApiResponse("Exam deleted successfully", false));
//        } else {
//            return ResponseEntity.status(404).body(new ApiResponse("Exam not found", true));
//        }
//    }
//
//    @Operation(summary = "Get questions by exam ID", description = "Retrieves all questions associated with a specific exam.")
//    @GetMapping("/{examId}/questions")
//    public ResponseEntity<ApiResponseWithMeta> getQuestionsByExamId(@PathVariable UUID examId) {
//        try {
//            ExamQuestionsByExamResponseDto response = examService.getQuestionsByExamId(examId);
//
//            Map<String, Object> meta = new HashMap<>();
//            meta.put("examId", response.getExamId());
//            meta.put("totalQuestions", response.getTotalQuestions());
//            meta.put("questions", response.getQuestions());
//
//            return ResponseEntity.ok(new ApiResponseWithMeta("Questions retrieved successfully", false, meta));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(new ApiResponseWithMeta(e.getMessage(), true, null));
//        }
//    }
//
//}
