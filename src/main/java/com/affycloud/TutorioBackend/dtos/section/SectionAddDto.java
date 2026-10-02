package com.affycloud.TutorioBackend.dtos.section;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SectionAddDto {
    private String title;
    private String description;
    private String sectionType;
}
