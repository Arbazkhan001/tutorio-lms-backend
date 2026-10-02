package com.affycloud.TutorioBackend.dtos.testSeries.exams;

import com.affycloud.TutorioBackend.enums.ExamType;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class TestSeriesUnifiedItemDto {
    private UUID id;
    private String title;
    private String description;
    /**
     * Use a common field to indicate the type. For tests, set it to "TEST"
     * and for exams, set it to "EXAM".
     */
    private ExamType examType;
    /**
     * If a test has createdAt/updatedAt but an exam does not,
     * then for exam objects these can be left null.
     */
    private Date createdAt;
    private Date updatedAt;
    /**
     * If both types have questions, you can map them into a common list.
     * If the question structure is different, you may need a unified question DTO.
     */
    private List<?> questions;
    private Boolean attended; 
    private Integer score;  
}

