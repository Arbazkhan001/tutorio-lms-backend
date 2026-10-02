package com.affycloud.TutorioBackend.models;

import com.affycloud.TutorioBackend.enums.ExamType;
import com.affycloud.TutorioBackend.enums.ItemType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tests")
@Data
public class Test {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 128)
    private String title;

    @Column(length = 10000)
    private String description;

    @JsonManagedReference
    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<Question> questions;

    @Column(nullable = true)
    private UUID courseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", length = 50) // Adjust length as needed
    private ExamType examType = ExamType.TEST;

    @Enumerated(EnumType.STRING)
    private ItemType itemType = ItemType.TEST;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_series_id")
    private TestSeries testSeries;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "directory_id", nullable = true)
    private Directory directory; // ✅ If test is inside a Directory

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User createdBy; 

    @Column
    @CreationTimestamp
    private Date createdAt;

    @Column
    @UpdateTimestamp
    private Date updatedAt;
}
