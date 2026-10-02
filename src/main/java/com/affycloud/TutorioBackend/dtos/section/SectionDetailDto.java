package com.affycloud.TutorioBackend.dtos.section;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SectionDetailDto {
    private UUID id;
    private String title;
    private String description;
    private String sectionType;
    private String thumbnail;
}