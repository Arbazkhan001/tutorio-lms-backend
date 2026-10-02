package com.affycloud.TutorioBackend.dtos;

import com.affycloud.TutorioBackend.models.IssuedCertificate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssuedCertificateResponseDto {
    private UUID id;
    private UUID enrollmentId;
    private UUID studentId;
    private String serialNumber;
    private String publicUrl;
    private String studentName;
    private String courseName;
    private String startDate;
    private String completionDate;
    private String issueDate;
    private Date issuedAt;
    private Date updatedAt;

    public static IssuedCertificateResponseDto fromEntity(IssuedCertificate cert) {
        return new IssuedCertificateResponseDto(
                cert.getId(),
                cert.getEnrollment().getId(),
                cert.getStudent().getId(),
                cert.getSerialNumber(),
                cert.getPublicUrl(),
                cert.getStudentName(),
                cert.getCourseName(),
                cert.getStartDate(),
                cert.getCompletionDate(),
                cert.getIssueDate(),
                cert.getIssuedAt(),
                cert.getUpdatedAt()
        );
    }
}
