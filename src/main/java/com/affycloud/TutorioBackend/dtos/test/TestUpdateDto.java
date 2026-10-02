package com.affycloud.TutorioBackend.dtos.test;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TestUpdateDto {
    private String title;
    private String description;
    private UUID courseType;
    private List<QuestionDto> questions;
}

