package com.affycloud.TutorioBackend.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "course_batches")
public class CourseBatch {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;

    private String description;

    private String url;

    private String batch;

    private Integer studentLimit;

    @Column(columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "course_attempt_id", nullable = false)
    private CourseAttempt courseAttempt;

    @CreationTimestamp
    @Column(updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

}

