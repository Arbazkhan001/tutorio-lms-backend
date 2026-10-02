package com.affycloud.TutorioBackend.dtos.exam;


import lombok.Data;

import java.util.List;

@Data
public class ExamDTO {
    private String title;
    private String description;
    private List<ExamQuestionAddDTO> questions;
}

