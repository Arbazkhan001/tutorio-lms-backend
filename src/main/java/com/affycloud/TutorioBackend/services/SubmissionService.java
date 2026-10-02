package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.submission.SubmissionDetailData;
import com.affycloud.TutorioBackend.models.Submission;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;

public interface SubmissionService {
          ApiResponse uploadAnswerSheet(UUID studentId, UUID examId, MultipartFile file)
                      throws IOException, GeneralSecurityException;
          ApiResponse uploadCheckedSheet(UUID submissionId, MultipartFile file, Integer score, Integer totalScore)
                      throws IOException, GeneralSecurityException;
          List<Submission> getSubmissionsByExamId(UUID examId);
          List<Submission> getSubmissionsByExamIdAndStudentId(UUID examId,UUID studentId);
          Submission getSubmissionById(UUID submissionId);
          List<Submission> getSubmissionsByStudentId(UUID studentId);
          Submission updateSubmissionScore(UUID submissionId, Integer score, Integer totalScore);
          void deleteSubmission(UUID submissionId);
          List<SubmissionDetailData> getAllSubmissions();

}
