package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.exam.ExamQuestionDTO;
import com.affycloud.TutorioBackend.models.ExamQuestion;

import java.util.List;
import java.util.UUID;

public interface QuestionService {
void addQuestionToExam(UUID examId, ExamQuestionDTO questionDTO);
List<ExamQuestion> getQuestionsByExamId(UUID examId);
}
