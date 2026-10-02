package com.affycloud.TutorioBackend.models;



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
public class Directory {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 256, nullable = false)
    private String name; // e.g., Maths, Accounts, Business

    private String thumbnail;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_series_id", nullable = false)
    private TestSeries testSeries; // ✅ Links directory to TestSeries

    @JsonManagedReference
    @OneToMany(mappedBy = "directory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Test> tests; // ✅ Tests inside the directory

    @JsonManagedReference
    @OneToMany(mappedBy = "directory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Exam> exams; // ✅ Exams inside the directory

    private Boolean isActive = true;

    @Column(updatable = false)
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;

}