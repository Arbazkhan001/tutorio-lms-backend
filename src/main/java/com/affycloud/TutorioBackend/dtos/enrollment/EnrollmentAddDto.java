package com.affycloud.TutorioBackend.dtos.enrollment;

import lombok.Data;

import java.util.UUID;

@Data
public class EnrollmentAddDto {
    private UUID studentId;
    private UUID itemId;
    private UUID batchId;
    private String itemType;
}
