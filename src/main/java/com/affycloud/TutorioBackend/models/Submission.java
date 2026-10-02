package com.affycloud.TutorioBackend.models;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;
@Data
@Entity
@Table(name = "submissions")
public class Submission {
    @Id
    @GeneratedValue
    private UUID id;

    private UUID studentId; 
    private UUID examId;
    private String answerSheetPdf; 
    private String checkedSheetPdf; 
    private Integer score; 
    private Integer totalScore; 


}
