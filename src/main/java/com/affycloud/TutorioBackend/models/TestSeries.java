package com.affycloud.TutorioBackend.models;


import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.enums.ContentType;
import com.affycloud.TutorioBackend.enums.ItemType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class TestSeries {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 512)
    private String title;

    @Column(length = 10000)
    private String description;

    @Column(length = 512)
    private Double price;

    @Column(length = 512)
    private String discountType;

    @Column(length = 512)
    private Double discount;

    @Column(length = 512)
    private Double finalPrice = 0.0;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType = PaymentType.FREE;

    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    @JsonManagedReference
    @OneToMany(mappedBy = "testSeries", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Test> tests;

    @JsonManagedReference
    @OneToMany(mappedBy = "testSeries", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Exam> exams;

    @JsonManagedReference
    @OneToMany(mappedBy = "testSeries", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Directory> directories;  // ✅ New field added for directories

    private String thumbnail;

    @Enumerated(EnumType.STRING)
    private ItemType itemType = ItemType.TESTSERIES;

    private Boolean isDeleted = false;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User createdBy;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

//    @Column(name = "created_by_id", nullable = false)
//    private UUID createdById;

    @Column(updatable = false)
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;

    public TestSeries(String title, String description, Double price, PaymentType paymentType, User createdBy) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.paymentType = paymentType;
        this.createdBy = createdBy;

    }
}
