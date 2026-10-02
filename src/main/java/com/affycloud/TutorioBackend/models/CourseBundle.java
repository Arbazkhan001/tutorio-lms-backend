package com.affycloud.TutorioBackend.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "course_bundles")
public class CourseBundle {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 10000)
    private String description;


    private Double price;
    private String discountType;
    private Double discount;
    private Double finalPrice = 0.0;



    @Column(length = 512)
    private String thumbnail; 

    @ManyToMany
    @JoinTable(
            name = "course_bundle_types",
            joinColumns = @JoinColumn(name = "bundle_id"),
            inverseJoinColumns = @JoinColumn(name = "course_type_id")
    )
    private List<CourseAttempt> courseAttempts;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "created_by_id", nullable = false)
    private UUID createdById;

    @Column
    @CreationTimestamp
    private Date createdAt;
}
