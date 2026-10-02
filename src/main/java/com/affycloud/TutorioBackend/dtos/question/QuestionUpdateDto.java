package com.affycloud.TutorioBackend.dtos.question;

import lombok.Data;

import java.util.List;

@Data
public class QuestionUpdateDto {
    private String questionText;
    private List<String> options;
    private Integer correctOptionIndex;
}
