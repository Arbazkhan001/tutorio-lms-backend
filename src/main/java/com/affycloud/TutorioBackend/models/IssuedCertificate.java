package com.affycloud.TutorioBackend.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "issued_certificates",
        uniqueConstraints = @UniqueConstraint(name = "uq_issued_certificate_enrollment", columnNames = "enrollment_id"))
public class IssuedCertificate {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false, unique = true)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Column(nullable = false, unique = true, length = 32)
    private String serialNumber;

    @Column(nullable = false, length = 1024)
    private String r2Key;

    @Column(nullable = false, length = 1024)
    private String publicUrl;

    // Snapshot of the values printed on the certificate at the time it was issued.
    @Column(length = 256)
    private String studentName;

    @Column(length = 256)
    private String courseName;

    @Column(length = 32)
    private String startDate;

    @Column(length = 32)
    private String completionDate;

    @Column(length = 32)
    private String issueDate;

    @Column
    @CreationTimestamp(source = SourceType.DB)
    private Date issuedAt;

    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;
}
