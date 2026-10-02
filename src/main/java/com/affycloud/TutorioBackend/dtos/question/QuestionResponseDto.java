package com.affycloud.TutorioBackend.dtos.question;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor  
@NoArgsConstructor  
public class QuestionResponseDto {
    private UUID id;
    private String questionText; 
    private List<String> options; 
    private int correctOptionIndex; 
}
