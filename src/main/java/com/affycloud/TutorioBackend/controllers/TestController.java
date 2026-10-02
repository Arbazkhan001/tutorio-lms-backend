package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.question.QuestionResponseDto;
import com.affycloud.TutorioBackend.dtos.question.QuestionUpdateDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.dtos.test.StudentTestSubmissionDto;
import com.affycloud.TutorioBackend.dtos.test.TestAddDto;
import com.affycloud.TutorioBackend.dtos.test.TestUpdateDto;
import com.affycloud.TutorioBackend.models.Test;
import com.affycloud.TutorioBackend.services.ResultService;
import com.affycloud.TutorioBackend.services.TestService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

        @Autowired
        private TestService testService;

        @Autowired
        private ResultService resultService;


        @Operation(summary = "Create a new test")
        @PostMapping("/create")
        public ResponseEntity<ApiResponse> createTest(
                @RequestBody TestAddDto request,
                @RequestHeader("creatorId") UUID creatorId) {
            try {
                ApiResponse response = testService.createTest(request, creatorId);
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException e) {
                // These errors represent user errors (e.g., user not found, invalid role, or course type not found)
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(e.getMessage(), true));
            } catch (Exception e) {
                // Catch-all for any unexpected errors.
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse("Unexpected error: " + e.getMessage(), true));
            }
        }


        @Operation(summary = "Submit test")
        @PostMapping("/submit")
        public ResponseEntity<ApiResponseWithMeta> submitTest(
                @RequestBody StudentTestSubmissionDto submission,
                @RequestParam("studentId") UUID studentId) {
            return ResponseEntity.ok(resultService.submitTest(submission, studentId));
        }

        @Operation(summary = "Get all tests")
        @GetMapping
        public List<Test> getAllTests() {
        return testService.getAllTests();
    }

        @Operation(summary = "Update an existing test")
        @PutMapping("/update/{testId}")
        public ResponseEntity<ApiResponse> updateTest(
                @PathVariable UUID testId,
                @RequestBody TestUpdateDto request) {
            try {
                ApiResponse response = testService.updateTest(testId, request);
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(e.getMessage(), true));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse("Unexpected error: " + e.getMessage(), true));
            }
        }

        @Operation(summary = "Get results of a test")
        @GetMapping("/{testId}/result")
        public ResponseEntity<Map<String, Object>> getTestResult(@PathVariable("testId") UUID testId,
                                                                 @RequestHeader("studentId") UUID studentId) {
            Map<String, Object> result = resultService.getTestResult(testId, studentId);
            return ResponseEntity.ok(result);
        }

        @Operation(summary = "Get tests by course type ID")
        @GetMapping("/by-course-type/{courseTypeId}")
        public ResponseEntity<ApiResponseWithMeta> getTestsByCourseType(
            @PathVariable UUID courseTypeId,
            @RequestParam(value = "studentId", required = false) UUID studentId) {
        ApiResponseWithMeta response = testService.getTestsByCourseType(courseTypeId, studentId);
        return ResponseEntity.ok(response);
        }

        @Operation(summary = "Get test by ID")
        @GetMapping("/{testId}")
        public ResponseEntity<ApiResponseWithMeta> getTestById(@PathVariable UUID testId) {
            ApiResponseWithMeta response = testService.getTestById(testId);
            return ResponseEntity.ok(response);
        }


        @Operation(summary = "Update an existing question")
        @PutMapping("/questions/update/{questionId}")
        public ResponseEntity<ApiResponse> updateQuestion(
                @PathVariable UUID questionId,
                @RequestBody QuestionUpdateDto request) {
            try {
                ApiResponse response = testService.updateQuestion(questionId, request);
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(e.getMessage(), true));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse("Unexpected error: " + e.getMessage(), true));
            }
        }

        @Operation(summary = "Get a question by ID")
        @GetMapping("/questions/{questionId}")
        public ResponseEntity<?> getQuestionById(@PathVariable UUID questionId) {
            try {
                QuestionResponseDto question = testService.getQuestionById(questionId);
                // Constructing the meta response
                Map<String, Object> meta = Map.of(
                    "id", question.getId(),
                    "questionText", question.getQuestionText(),
                    "options", question.getOptions(),
                    "correctOptionIndex", question.getCorrectOptionIndex()
                );
                ApiResponseWithMeta response = new ApiResponseWithMeta("Question retrieved successfully", false, meta);
                return ResponseEntity.ok(response);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseWithMeta(e.getMessage(), true, null));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponseWithMeta("Unexpected error: " + e.getMessage(), true, null));
            }
        }

}
