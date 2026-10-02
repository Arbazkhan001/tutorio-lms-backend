package com.affycloud.TutorioBackend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;
import java.util.UUID;
@Entity
@Table(name = "questions")
@Data
public class Question {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 4000)
    private String questionText;

    @ElementCollection
    @CollectionTable(name = "options", joinColumns = @JoinColumn(name = "question_id"))
    private List<String> options;

    @Column
    private Integer correctOptionIndex; 

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id")
    private Test test;

    @Column
    @CreationTimestamp
    private Date createdAt;

    @Column
    @UpdateTimestamp
    private Date updatedAt;
}
