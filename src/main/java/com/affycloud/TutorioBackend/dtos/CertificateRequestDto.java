package com.affycloud.TutorioBackend.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class CertificateRequestDto {
    /** The enrollment (student + course) the certificate is being generated for. */
    private UUID enrollmentId;

    /** Optional, yyyy-MM-dd. Defaults to today if not supplied. */
    private String completionDate;
}
