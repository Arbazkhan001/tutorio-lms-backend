package com.affycloud.TutorioBackend.dtos.exam;


import lombok.Data;

import java.util.UUID;

@Data
public class ExamQuestionDTO {
    private UUID id;
    private String questionText;
}

