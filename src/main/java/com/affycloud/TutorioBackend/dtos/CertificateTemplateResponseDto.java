package com.affycloud.TutorioBackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CertificateTemplateResponseDto {
    private UUID id;
    private String originalFilename;
    private String publicUrl;
    private Date uploadedAt;
}
