package com.affycloud.TutorioBackend.models;

import com.affycloud.TutorioBackend.enums.EnrollmentStatus;
import com.affycloud.TutorioBackend.enums.ItemType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    @ManyToOne
    @JoinColumn(name = "course_attempt_id")
    private CourseAttempt courseAttempt;

    @ManyToOne
    @JoinColumn(name = "ebook_id")
    private Ebook ebook;

    @ManyToOne
    @JoinColumn(name = "test_series_id")
    private TestSeries testSeries;

    @ManyToOne
    @JoinColumn(name = "course_batch_id")
    private CourseBatch courseBatch;

    @Column(nullable = false)
    @CreationTimestamp(source = SourceType.DB)
    private Date enrolledAt;

    /** Computed once at purchase time and stored — never recalculated, so plan changes don't retroactively affect existing students. Null = lifetime access. */
    @Column(name = "enrollment_end_at")
    private Date enrollmentEndAt;

    /** Lifecycle status — rows are never hard-deleted so purchase history is preserved. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;

    private Boolean isBlocked = false;
}
