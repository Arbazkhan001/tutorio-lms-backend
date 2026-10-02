//package com.affycloud.TutorioBackend.controllers;
//
//import com.affycloud.TutorioBackend.dtos.exam.ExamQuestionDTO;
//import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
//import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
//import com.affycloud.TutorioBackend.dtos.submission.SubmissionDetailData;
//import com.affycloud.TutorioBackend.dtos.submission.exams.ExamQuestionsAndSubmissionByExamResponseDto;
//import com.affycloud.TutorioBackend.models.ExamQuestion;
//import com.affycloud.TutorioBackend.models.Submission;
//import com.affycloud.TutorioBackend.services.QuestionService;
//import com.affycloud.TutorioBackend.services.SubmissionService;
//import io.swagger.v3.oas.annotations.Operation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/questions")
//public class QuestionController {
//
//    @Autowired
//    private QuestionService questionService;
//
//    @Autowired
//    private SubmissionService submissionService;
//
//    @Operation(summary = "Add a question to an exam")
//    @PostMapping("/add/{examId}")
//    public ResponseEntity<ApiResponse> addQuestion(@PathVariable UUID examId, @RequestBody ExamQuestionDTO questionDTO) {
//        questionService.addQuestionToExam(examId, questionDTO);
//        ApiResponse response = new ApiResponse("Question added successfully", false);
//        return ResponseEntity.ok(response);
//    }
//
//    @Operation(summary = "Get all questions by exam ID")
//    @GetMapping("/exam/{examId}/student/{studentId}")
//    public ResponseEntity<?> getQuestionsByExamId(@PathVariable UUID examId,@PathVariable UUID studentId) {
//      try {
//        List<ExamQuestion> questions = questionService.getQuestionsByExamId(examId);
//        List<Submission> submissions = submissionService.getSubmissionsByExamIdAndStudentId(examId,studentId);
//
//        List<ExamQuestionDTO> questionDtos = questions.stream()
//                .map(q -> {
//                  ExamQuestionDTO dto = new ExamQuestionDTO();
//                    dto.setId(q.getId());
//                    dto.setQuestionText(q.getQuestionText());
//                    return dto;
//                })
//                .collect(Collectors.toList());
//
//          // Instead of a list, pick the first submission if available.
//          SubmissionDetailData submissionDto = null;
//          if (!submissions.isEmpty()) {
//              Submission s = submissions.get(0);
//              submissionDto = new SubmissionDetailData();
//              submissionDto.setId(s.getId());
//              submissionDto.setStudentId(s.getStudentId());
//              submissionDto.setExamId(s.getExamId());
//              submissionDto.setAnswerSheetPdf(s.getAnswerSheetPdf());
//              submissionDto.setCheckedSheetPdf(s.getCheckedSheetPdf());
//              submissionDto.setScore(s.getScore());
//              submissionDto.setTotalScore(s.getTotalScore());
//          }
//          ExamQuestionsAndSubmissionByExamResponseDto examQuestionResponse = new ExamQuestionsAndSubmissionByExamResponseDto(examId, questionDtos,submissionDto);
//            ApiResponseWithMeta response = new ApiResponseWithMeta("Questions retrieved successfully", false, Map.of("data", examQuestionResponse));
//            return ResponseEntity.ok(response);
//          } catch (ResponseStatusException ex) {
//              return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                      .body(new ApiResponse(ex.getReason(), true));
//          } catch (Exception e) {
//              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                      .body(new ApiResponse("Unexpected error: " + e.getMessage(), true));
//          }
//        }
//
//}
