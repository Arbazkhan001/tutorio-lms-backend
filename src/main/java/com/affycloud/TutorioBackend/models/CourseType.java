package com.affycloud.TutorioBackend.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "course_types")
public class CourseType {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 10000)
    private String description;

    @Column(length = 128)
    private String courseType;

    @JsonIgnore
    @OneToMany(mappedBy = "courseType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseAttempt> courseAttempts = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "courseType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subject> subjects = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnore
    private Course course;

    @Column
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;

     /* Other helper methods for variants
     Helper method to add size*/
    public void addVariants(CourseAttempt courseAttempt) {
        courseAttempts.add(courseAttempt);
        courseAttempt.setCourseType(this);
    }

     /*Helper method to remove size*/
    public void removeSize(CourseAttempt courseAttempt) {
        courseAttempts.remove(courseAttempt);
        courseAttempt.setCourseType(null);
    }
}
