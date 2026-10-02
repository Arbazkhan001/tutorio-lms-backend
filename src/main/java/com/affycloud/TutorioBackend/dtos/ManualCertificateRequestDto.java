package com.affycloud.TutorioBackend.dtos;

import lombok.Data;

/**
 * Fields for a one-off certificate that is not tied to any LMS student/enrollment record.
 * Dates are expected as yyyy-MM-dd; completionDate defaults to today if blank.
 */
@Data
public class ManualCertificateRequestDto {
    private String studentName;
    private String courseName;
    private String startDate;
    private String completionDate;
}
