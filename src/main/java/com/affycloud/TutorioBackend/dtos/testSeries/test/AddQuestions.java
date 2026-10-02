package com.affycloud.TutorioBackend.dtos.testSeries.test;

import lombok.Data;

import java.util.List;

@Data
public class AddQuestions {
    private String questionText;
    private List<String> options;
    private Integer correctOptionIndex;
}
