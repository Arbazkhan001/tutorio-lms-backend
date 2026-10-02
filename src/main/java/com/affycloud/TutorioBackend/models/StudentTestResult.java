package com.affycloud.TutorioBackend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.UUID;
@Entity
@Table(name = "student_test_results")
@Data
public class StudentTestResult {

    @Id
    @GeneratedValue
    private UUID id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id")
    private Test test; 

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student; 

    @Column
    private Integer totalQuestions;

    @Column
    private Integer correctAnswers;

    @Column
    private Integer score;

    @Column
    @CreationTimestamp
    private Date takenAt;
}
