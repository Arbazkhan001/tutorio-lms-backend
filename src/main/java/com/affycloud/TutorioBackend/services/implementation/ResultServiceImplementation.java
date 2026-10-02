package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.dtos.test.StudentTestSubmissionDto;
import com.affycloud.TutorioBackend.models.Question;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.models.StudentTestResult;
import com.affycloud.TutorioBackend.models.Test;
import com.affycloud.TutorioBackend.repositories.StudentRepository;
import com.affycloud.TutorioBackend.repositories.StudentTestResultRepository;
import com.affycloud.TutorioBackend.repositories.TestRepository;
import com.affycloud.TutorioBackend.services.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ResultServiceImplementation implements ResultService {
    
    @Autowired
    private TestRepository testRepository;

    @Autowired
    private StudentRepository studentRepository;
    
    @Autowired
    private StudentTestResultRepository resultRepository;


    @Override
    public ApiResponseWithMeta submitTest(StudentTestSubmissionDto submission, UUID studentId) {
        Test test = testRepository.findById(submission.getTestId())
                .orElseThrow(() -> new IllegalArgumentException("Test not found."));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));

        // Check if the student has already taken this test
        Optional<StudentTestResult> existingResult = resultRepository.findByStudentIdAndTestId(studentId, submission.getTestId());

        List<UUID> unansweredQuestions = test.getQuestions().stream()
                .filter(q -> !submission.getAnswers().containsKey(q.getId()))
                .map(Question::getId)
                .toList();

        int totalQuestions = test.getQuestions().size();
        int answeredQuestions = totalQuestions - unansweredQuestions.size();
        int correctAnswers = (int) test.getQuestions().stream()
                .filter(q -> {
                    Integer studentAnswerIndex = submission.getAnswers().get(q.getId());
                    return studentAnswerIndex != null && studentAnswerIndex.equals(q.getCorrectOptionIndex());
                })
                .count();

        int score = totalQuestions > 0 ? (correctAnswers * 100) / totalQuestions : 0;

        StudentTestResult result;
        if (existingResult.isPresent()) {
            // Update existing result
            result = existingResult.get();
            result.setCorrectAnswers(correctAnswers);
            result.setScore(score);
            result.setTotalQuestions(totalQuestions);
        } else {
            // Create new result
            result = new StudentTestResult();
            result.setTest(test);
            result.setStudent(student);
            result.setTotalQuestions(totalQuestions);
            result.setCorrectAnswers(correctAnswers);
            result.setScore(score);
        }

        resultRepository.save(result);

        Map<String, Object> metaData = Map.of(
                "score", score,
                "totalQuestions", totalQuestions,
                "answeredQuestions", answeredQuestions,
                "unansweredQuestions", unansweredQuestions.size(),
                "correctAnswers", correctAnswers
        );

        return new ApiResponseWithMeta("Test submitted successfully.", false, metaData);
    }

    @Override
    public Map<String, Object> getTestResult(UUID testId, UUID studentId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("Test not found."));
    
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found."));
    
        StudentTestResult result = resultRepository.findByTestAndStudent(test, student)
                .orElseThrow(() -> new IllegalArgumentException("Test result not found for the student."));
    
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("studentId", student.getId());
        resultMap.put("studentName", student.getName());
        resultMap.put("score", result.getScore());
        resultMap.put("totalQuestions", result.getTotalQuestions());
        resultMap.put("correctAnswers", result.getCorrectAnswers());
    
        return resultMap;
    }

}
