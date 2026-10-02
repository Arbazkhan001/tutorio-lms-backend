package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.exam.ExamQuestionDTO;
import com.affycloud.TutorioBackend.models.Exam;
import com.affycloud.TutorioBackend.models.ExamQuestion;
import com.affycloud.TutorioBackend.repositories.*;
import com.affycloud.TutorioBackend.services.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;


@Service
public class QuestionServiceImplementation implements QuestionService {

    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private ExamRepository examRepository;

    @Override
     public void addQuestionToExam(UUID examId, ExamQuestionDTO questionDTO) {
        Exam exam = examRepository.findById(examId).orElseThrow(() -> new RuntimeException("Exam not found"));

        ExamQuestion question = new ExamQuestion();
        question.setQuestionText(questionDTO.getQuestionText());
        question.setExam(exam);
        questionRepository.save(question);
    }

    @Override
    public List<ExamQuestion> getQuestionsByExamId(UUID examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Exam not found."));

        return questionRepository.findByExam(exam);
    }

}