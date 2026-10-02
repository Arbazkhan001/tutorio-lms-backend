package com.affycloud.TutorioBackend.dtos.exam;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ExamQuestionsByExamResponseDto {
    private UUID examId;
    private int totalQuestions;
    private List<ExamQuestionDTO> questions;

    public ExamQuestionsByExamResponseDto(UUID examId, List<ExamQuestionDTO> questions) {
        this.examId = examId;
        this.totalQuestions = questions.size();
        this.questions = questions;
    }
}
