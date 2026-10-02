package com.affycloud.TutorioBackend.dtos.course.courseType;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ChapterSummaryDto {
    private UUID id;
    private String name;
    private List<VideoSummaryDto> videos;
}
