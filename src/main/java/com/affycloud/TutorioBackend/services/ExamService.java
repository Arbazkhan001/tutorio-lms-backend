package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.exam.ExamDTO;
import com.affycloud.TutorioBackend.dtos.exam.ExamQuestionsByExamResponseDto;
import com.affycloud.TutorioBackend.dtos.exam.ExamResponseDTO;

import java.util.List;
import java.util.UUID;

public interface ExamService {
    ExamResponseDTO createExam(ExamDTO request);
    List<ExamResponseDTO> getAllExams();
    boolean deleteExam(UUID id);
    ExamResponseDTO getExamById(UUID id);
    boolean updateExam(UUID id, ExamDTO examDTO);
    ExamQuestionsByExamResponseDto getQuestionsByExamId(UUID examId);
}
