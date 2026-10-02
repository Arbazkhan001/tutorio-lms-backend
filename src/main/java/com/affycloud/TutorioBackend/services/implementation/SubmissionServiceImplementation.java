//package com.affycloud.TutorioBackend.services.implementation;
//
//import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
//import com.affycloud.TutorioBackend.dtos.submission.SubmissionDetailData;
//import com.affycloud.TutorioBackend.enums.NotificationType;
//import com.affycloud.TutorioBackend.enums.Role;
//import com.affycloud.TutorioBackend.models.Exam;
//import com.affycloud.TutorioBackend.models.Student;
//import com.affycloud.TutorioBackend.models.Submission;
//import com.affycloud.TutorioBackend.repositories.ExamRepository;
//import com.affycloud.TutorioBackend.repositories.StudentRepository;
//import com.affycloud.TutorioBackend.repositories.SubmissionRepository;
//import com.affycloud.TutorioBackend.services.NotificationService;
//import com.affycloud.TutorioBackend.services.SubmissionService;
//import com.affycloud.TutorioBackend.services.implementation.drive.GoogleDriveExamSheetService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.io.IOException;
//import java.security.GeneralSecurityException;
//import java.util.List;
//import java.util.NoSuchElementException;
//import java.util.UUID;
//
//@Service
//public class SubmissionServiceImplementation implements SubmissionService {
//
//    @Autowired
//    private GoogleDriveExamSheetService googleDriveExamSheetService;
//
//    @Autowired
//    private SubmissionRepository submissionRepository;
//
//    @Autowired
//    private StudentRepository studentRepository;
//
//    @Autowired
//    private ExamRepository examRepository;
//
//    @Autowired
//    private NotificationService notificationService;
//
//    @Override
//    public ApiResponse uploadAnswerSheet(UUID studentId, UUID examId, MultipartFile file)
//            throws IOException, GeneralSecurityException {
//
//        boolean exists = submissionRepository.existsByStudentIdAndExamId(studentId, examId);
//        if (exists) {
//            return new ApiResponse("You have already submitted an answer sheet for this exam.", true);
//        }
//
//        Student student = studentRepository.findById(studentId)
//                .orElseThrow(() -> new NoSuchElementException("Student not found with ID: " + studentId));
//
//
//        String fileUrl = googleDriveExamSheetService.uploadExamSheet(file);
//
//        Submission submission = new Submission();
//        submission.setStudentId(studentId);
//        submission.setExamId(examId);
//        submission.setAnswerSheetPdf(fileUrl);
//
//        submissionRepository.save(submission);
//
//        // Only if student attempts exam and  uploads answer-sheet , send notification to admin
//        notificationService.createNotification(
//                submission.getId(),
//                NotificationType.SUBMISSION,
//                null,
//                Role.ADMIN, // ✅ Correct: ADMIN ko bhejna hai
//                "New Exam Submission from " + student.getName(),
//                "{ examName : " + submission.getExamId() + " }"
//        );
//
//        return new ApiResponse("Answer sheet uploaded successfully", false);
//    }
//
//    @Override
//    public ApiResponse uploadCheckedSheet(UUID submissionId, MultipartFile file, Integer score, Integer totalScore)
//            throws IOException, GeneralSecurityException {
//
//        Submission submission = submissionRepository.findById(submissionId)
//                .orElseThrow(() -> new RuntimeException("Submission not found"));
//
//        if (submission.getCheckedSheetPdf() != null) {
//            return new ApiResponse("Checked sheet has already been uploaded for this submission.", true);
//        }
//
//        String fileUrl = googleDriveExamSheetService.uploadExamSheet(file);
//        submission.setCheckedSheetPdf(fileUrl);
//
//        if (score != null) {
//            submission.setScore(score);
//        }
//        if (totalScore != null) {
//            submission.setTotalScore(totalScore);
//        }
//
//        Student student = studentRepository.findById(submission.getStudentId())
//                .orElseThrow(() -> new RuntimeException("Student not found."));
//
//        submissionRepository.save(submission);
//        // Only if admin checks answer sheet and submit checked sheet, send notification to student
//        notificationService.createNotification(
//                submission.getId(),
//                NotificationType.SUBMISSION,
//                student,
//                Role.STUDENT, // ✅ Correct: STUDENT ko bhejna hai
//                "Exam Checked notification from Admin" ,
//                "{ your score is " + submission.getScore() + " From " +submission.getTotalScore() + " }"
//        );
//
//        return new ApiResponse("Checked sheet uploaded successfully", false);
//    }
//
//    @Override
//    public List<Submission> getSubmissionsByExamId(UUID examId) {
//        return submissionRepository.findByExamId(examId);
//    }
//
//    @Override
//    public List<Submission> getSubmissionsByExamIdAndStudentId(UUID examId,UUID studentId) {
//
//        // check if student exist or not
//        examRepository.findById(examId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
//                        "Exam not found."));
//
//        // check if student exist or not
//        studentRepository.findById(studentId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
//                        "Student not found."));
//
//        return submissionRepository.findByStudentIdAndExamId(studentId,examId);
//    }
//
//
//    @Override
//    public Submission getSubmissionById(UUID submissionId) {
//        return submissionRepository.findById(submissionId)
//                .orElseThrow(() -> new RuntimeException("Submission not found"));
//    }
//
//    @Override
//    public List<Submission> getSubmissionsByStudentId(UUID studentId) {
//        return submissionRepository.findByStudentId(studentId);
//    }
//
//    @Override
//    public Submission updateSubmissionScore(UUID submissionId, Integer score, Integer totalScore) {
//        Submission submission = getSubmissionById(submissionId);
//        submission.setScore(score);
//        submission.setTotalScore(totalScore);
//        return submissionRepository.save(submission);
//    }
//
//    @Override
//    public void deleteSubmission(UUID submissionId) {
//        submissionRepository.deleteById(submissionId);
//    }
//
//    @Override
//public List<SubmissionDetailData> getAllSubmissions() {
//    List<Submission> submissions = submissionRepository.findAll();
//
//        List<SubmissionDetailData> submissionDetailData = submissions.stream().map(submission -> {
//        SubmissionDetailData data = new SubmissionDetailData();
//            Student student = studentRepository.findById(submission.getStudentId())
//                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found."));
//            Exam exam = examRepository.findById(submission.getExamId())
//                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found."));
//
//            data.setId(submission.getId());
//            data.setStudentId(submission.getStudentId());
//            data.setStudentName(student.getName());
//            data.setExamId(submission.getExamId());
//            data.setExamName(exam.getTitle());
//            data.setAnswerSheetPdf(submission.getAnswerSheetPdf());
//            data.setCheckedSheetPdf(submission.getCheckedSheetPdf());
//            data.setScore(submission.getScore());
//            data.setTotalScore(submission.getTotalScore());
//            return data;
//    }).toList();
//        return  submissionDetailData;
//}
//
//}
