package com.affycloud.TutorioBackend.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "examQuestion")
public class ExamQuestion {
    @Id
    @GeneratedValue
    private UUID id;

    private String questionText; // Question ka content

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    @JsonBackReference
    private Exam exam;

    // Getters & Setters
}
