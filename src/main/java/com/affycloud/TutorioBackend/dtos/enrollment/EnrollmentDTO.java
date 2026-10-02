package com.affycloud.TutorioBackend.dtos.enrollment;

import com.affycloud.TutorioBackend.models.Enrollment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentDTO {
    private UUID id;
    private String itemType;
    private String enrolledAt;
    private CourseAttemptForEnrollmentDTO courseAttempt;
    private EbookForEnrollmentDTO ebook;
    private TestSeriesForEnrollmentDTO testSeries;
    private CourseBatchForEnrollmentDTO courseBatch;
    private Boolean isBlocked;
    private String enrollmentEndAt;
    private String studentName;
    private String studentPhone;
    private String countryCode;

    public static EnrollmentDTO fromEntity(Enrollment enrollment) {
        String enrollmentEndAtStr = null;
        if (enrollment.getEnrolledAt() != null && enrollment.getCourseAttempt() != null && enrollment.getCourseAttempt().getExpirationDays() != null) {
            long daysInMillis = enrollment.getCourseAttempt().getExpirationDays() * 24L * 60L * 60L * 1000L;
            java.sql.Timestamp timestamp = new java.sql.Timestamp(enrollment.getEnrolledAt().getTime() + daysInMillis);
            if (enrollment.getEnrolledAt() instanceof java.sql.Timestamp) {
                timestamp.setNanos(((java.sql.Timestamp) enrollment.getEnrolledAt()).getNanos());
            }
            enrollmentEndAtStr = timestamp.toString();
        }

        String stName = enrollment.getStudent() != null ? enrollment.getStudent().getName() : null;
        String stPhone = enrollment.getStudent() != null ? enrollment.getStudent().getPhoneNumber() : null;
        String cCode = enrollment.getStudent() != null ? enrollment.getStudent().getCountryCode() : null;

        return new EnrollmentDTO(
                enrollment.getId(),
                enrollment.getItemType().name(),
                enrollment.getEnrolledAt() != null ? enrollment.getEnrolledAt().toString() : null,
                enrollment.getCourseAttempt() != null
                        ? CourseAttemptForEnrollmentDTO.fromEntity(enrollment.getCourseAttempt())
                        : null,
                enrollment.getEbook() != null ? EbookForEnrollmentDTO.fromEntity(enrollment.getEbook()) : null,
                enrollment.getTestSeries() != null ? TestSeriesForEnrollmentDTO.fromEntity(enrollment.getTestSeries())
                        : null,
                enrollment.getCourseBatch() != null
                        ? CourseBatchForEnrollmentDTO.fromEntity(enrollment.getCourseBatch())
                        : null,
                enrollment.getIsBlocked(),
                enrollmentEndAtStr,
                stName,
                stPhone,
                cCode
        );
    }
}
