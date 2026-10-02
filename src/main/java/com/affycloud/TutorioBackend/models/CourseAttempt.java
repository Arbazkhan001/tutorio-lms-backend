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
@Table(name = "course_attempts")
public class CourseAttempt {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 512)
    private String batch;

    @Column(length = 512)
    private String plan;

    @Column(length = 512)
    private Boolean hasBatch = false;

    @Column(columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date attempt; // Change from String to Date

    @Column(length = 512)
    private double price;

    @Column(length = 512)
    private String discountType;

    @Column(length = 512)
    private double discount;

    @Column(length = 512)
    private double finalPrice = 0.0;


    @ManyToOne
    @JoinColumn(name = "course_type_id", nullable = false)
    @JsonIgnore
    private CourseType courseType;

    @OneToMany(mappedBy = "courseAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    @OrderBy("createdAt DESC")
    private List<CourseBatch> courseBatches = new ArrayList<>();


    @Column
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    @Column(name = "expiration_days")
    private Integer expirationDays = 30; // e.g., 1 = expires after 1 day of enrollment

    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;


    /* Other helper methods for variants
    Helper method to add size*/
    public void addVariants(CourseBatch courseBatch) {
        courseBatches.add(courseBatch);
        courseBatch.setCourseAttempt(this);
    }

    /*Helper method to remove size*/
    public void removeSize(CourseBatch courseBatch) {
        courseBatches.remove(courseBatch);
        courseBatch.setCourseAttempt(null);
    }
}


