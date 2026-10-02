package com.affycloud.TutorioBackend.dtos.test;

import com.affycloud.TutorioBackend.dtos.question.QuestionDto;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TestAddDto {
    private String title;
    private String description;
    private List<QuestionDto> questions;
    private UUID courseType; // Optional field

}
