package com.affycloud.TutorioBackend.dtos.question;

import lombok.Data;

import java.util.List;

@Data
public class QuestionDto {
    private String questionText;
    private List<String> options;
    private Integer correctOptionIndex;
}
