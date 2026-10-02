package com.affycloud.TutorioBackend.dtos.submission;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class UploadCheckedSheetRequestDto {
    @NotNull(message = "Submission ID is required")
    private UUID submissionId;

    @NotNull(message = "File is required")
    private MultipartFile file;

    private Integer score;
    private Integer totalScore;
}