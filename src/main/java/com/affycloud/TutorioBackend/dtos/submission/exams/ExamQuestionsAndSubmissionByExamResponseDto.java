package com.affycloud.TutorioBackend.dtos.submission.exams;

import com.affycloud.TutorioBackend.dtos.exam.ExamQuestionDTO;
import com.affycloud.TutorioBackend.dtos.submission.SubmissionDetailData;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ExamQuestionsAndSubmissionByExamResponseDto {
    private UUID examId;
    private int totalQuestions;
    private List<ExamQuestionDTO> questions;
    private SubmissionDetailData submissions;

    public ExamQuestionsAndSubmissionByExamResponseDto(UUID examId, List<ExamQuestionDTO> questions,SubmissionDetailData submissions) {
        this.examId = examId;
        this.totalQuestions = questions.size();
        this.questions = questions;
        this.submissions = submissions;
    }
}
