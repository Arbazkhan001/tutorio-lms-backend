package com.affycloud.TutorioBackend.models;


import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.enums.ItemType;
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
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 128)
    private String title;

    @Column(length = 128)
    private String collection;

    @Column(length = 128)
    private String category;

    @Column(length = 128)
    private String subject;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType = PaymentType.FREE;

    @Enumerated(EnumType.STRING)
    private ItemType itemType = ItemType.COURSE;

    private String thumbnail;

    private Boolean isActive = true;
    private Boolean isPopular = false;
    private Boolean onBanner = false;

    @JsonIgnore
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseType> courseTypes = new ArrayList<>();

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "created_by_id", nullable = false)
    private UUID createdById;

    @Column
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;

    // Other helper methods for variants
    // Helper method to add size
    public void addVariants(CourseType courseType) {
        courseTypes.add(courseType);
        courseType.setCourse(this);
    }

    // Helper method to remove size
    public void removeSize(CourseType courseType) {
        courseTypes.remove(courseType);
        courseType.setCourse(null);
    }
}


