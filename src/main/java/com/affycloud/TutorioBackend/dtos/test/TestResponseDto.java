package com.affycloud.TutorioBackend.dtos.test;

import com.affycloud.TutorioBackend.dtos.question.QuestionResponseDto;
import com.affycloud.TutorioBackend.enums.ExamType;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class TestResponseDto {
    private UUID id; 
    private String title; 
    private String description;
    private ExamType examType;
    private Date createdAt; 
    private Date updatedAt; 
    private List<QuestionResponseDto> questions; 
}
