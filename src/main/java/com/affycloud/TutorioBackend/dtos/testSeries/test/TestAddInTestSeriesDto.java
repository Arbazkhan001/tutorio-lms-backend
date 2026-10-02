package com.affycloud.TutorioBackend.dtos.testSeries.test;


import lombok.Data;

import java.util.List;

@Data
public class TestAddInTestSeriesDto {
    private String title;
    private String description;
    private List<AddQuestions> questions;
}