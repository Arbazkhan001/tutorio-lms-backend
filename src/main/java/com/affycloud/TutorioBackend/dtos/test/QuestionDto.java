package com.affycloud.TutorioBackend.dtos.test;

import lombok.Data;

import java.util.List;
import java.util.UUID;
@Data
public class QuestionDto {
    private UUID id; 
    private String questionText;
    private List<String> options;
    private int correctOptionIndex;
}
