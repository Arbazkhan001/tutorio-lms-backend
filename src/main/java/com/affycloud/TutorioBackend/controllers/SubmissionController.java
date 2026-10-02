//package com.affycloud.TutorioBackend.controllers;
//
//import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
//import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
//import com.affycloud.TutorioBackend.dtos.submission.SubmissionDetailData;
//import com.affycloud.TutorioBackend.dtos.submission.UploadAnswerSheetRequestDto;
//import com.affycloud.TutorioBackend.dtos.submission.UploadCheckedSheetRequestDto;
//import com.affycloud.TutorioBackend.models.Submission;
//import com.affycloud.TutorioBackend.services.SubmissionService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.IOException;
//import java.security.GeneralSecurityException;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/submissions")
//@Tag(name = "Submission Controller", description = "APIs for handling exam submissions")
//public class SubmissionController {
//
//    @Autowired
//    private SubmissionService submissionService;
//
//    @Operation(summary = "Upload an answer sheet for an exam", description = "Allows students to upload their answer sheets as a PDF file.")
//    @PostMapping("/upload-answer-sheet")
//    public ResponseEntity<ApiResponse> uploadAnswerSheet(@ModelAttribute UploadAnswerSheetRequestDto requestDto) {
//        try {
//            ApiResponse response = submissionService.uploadAnswerSheet(
//                    requestDto.getStudentId(), requestDto.getExamId(), requestDto.getFile()
//            );
//            return ResponseEntity.ok(response);
//        } catch (IOException | GeneralSecurityException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse("Error uploading answer sheet: " + e.getMessage(), true));
//        }
//    }
//
//    @Operation(summary = "Upload checked answer sheet", description = "Allows teachers to upload the checked answer sheet along with the score.")
//    @PostMapping("/upload-checked")
//    public ResponseEntity<ApiResponse> uploadCheckedSheet(
//            @ModelAttribute @Valid UploadCheckedSheetRequestDto requestDto) {
//        try {
//            ApiResponse response = submissionService.uploadCheckedSheet(
//                    requestDto.getSubmissionId(),
//                    requestDto.getFile(),
//                    requestDto.getScore(),
//                    requestDto.getTotalScore()
//            );
//            return ResponseEntity.ok(response);
//        } catch (IOException | GeneralSecurityException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse("Error uploading checked sheet: " + e.getMessage(), true));
//        }
//    }
//
//    @Operation(summary = "Get submissions for an exam", description = "Fetches all submissions made for a given exam.")
//    @GetMapping("/exam/{examId}")
//    public ResponseEntity<?> getSubmissionsByExamId(@PathVariable UUID examId) {
//        try {
//            List<Submission> submissions = submissionService.getSubmissionsByExamId(examId);
//
//            Map<String, Object> meta = Map.of(
//                    "examId", examId,
//                    "totalSubmissions", submissions.size(),
//                    "submissions", submissions
//            );
//
//            ApiResponseWithMeta response = new ApiResponseWithMeta("Submissions retrieved successfully", false, meta);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponseWithMeta("Unexpected error: " + e.getMessage(), true, null));
//        }
//    }
//
//    @Operation(summary = "Get submission by ID", description = "Retrieve a specific submission using its ID.")
//    @GetMapping("/{submissionId}")
//    public ResponseEntity<Submission> getSubmissionById(@PathVariable UUID submissionId) {
//        return ResponseEntity.ok(submissionService.getSubmissionById(submissionId));
//    }
//
//    @Operation(summary = "Get submissions by student ID", description = "Retrieve all submissions of a specific student.")
//    @GetMapping("/student/{studentId}")
//    public ResponseEntity<List<Submission>> getSubmissionsByStudentId(@PathVariable UUID studentId) {
//        return ResponseEntity.ok(submissionService.getSubmissionsByStudentId(studentId));
//    }
//
//    @Operation(summary = "Update submission score", description = "Update the score of a submission.")
//    @PatchMapping("/{submissionId}/update-score")
//    public ResponseEntity<Submission> updateSubmissionScore(
//            @PathVariable UUID submissionId,
//            @RequestParam Integer score,
//            @RequestParam Integer totalScore) {
//        return ResponseEntity.ok(submissionService.updateSubmissionScore(submissionId, score, totalScore));
//    }
//
//    @Operation(summary = "Delete a submission", description = "Delete a submission using its ID.")
//    @DeleteMapping("/{submissionId}")
//    public ResponseEntity<?> deleteSubmission(@PathVariable UUID submissionId) {
//        submissionService.deleteSubmission(submissionId);
//        return ResponseEntity.ok().body("Submission deleted successfully");
//    }
//
//    @Operation(summary = "Get all submissions", description = "Fetches all submissions.")
//    @GetMapping("/List")
//    public ResponseEntity<ApiResponseWithMeta> getAllSubmissions() {
//        try {
//            List<SubmissionDetailData> submissions = submissionService.getAllSubmissions();
//
//            Map<String, Object> meta = Map.of(
//                    "totalSubmissions", submissions.size(),
//                    "submissions", submissions
//            );
//
//            return ResponseEntity.ok(new ApiResponseWithMeta("Submissions retrieved successfully", false, meta));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponseWithMeta("Unexpected error: " + e.getMessage(), true, null));
//        }
//    }
//
//}
