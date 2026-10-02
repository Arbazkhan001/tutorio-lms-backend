package com.affycloud.TutorioBackend.dtos.exam;

import com.affycloud.TutorioBackend.dtos.exam.ExamQuestionResponseDTO;
import com.affycloud.TutorioBackend.enums.ExamType;
import lombok.Data;

import java.util.List;
import java.util.UUID;

    @Data
    public class ExamResponseDTO {
        private UUID id;
        private String title;
        private String description;
        private ExamType examType;
        private List<ExamQuestionResponseDTO> questions;
    }
