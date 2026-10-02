package com.affycloud.TutorioBackend.dtos.submission;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class UploadAnswerSheetRequestDto {
    private UUID studentId;
    private UUID examId;
    private MultipartFile file;
}
