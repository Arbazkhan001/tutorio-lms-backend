package com.affycloud.TutorioBackend.dtos.exam;

import lombok.Data;

import java.util.UUID;

@Data
public class ExamQuestionResponseDTO {
    private UUID id;
    private String questionText;
}
