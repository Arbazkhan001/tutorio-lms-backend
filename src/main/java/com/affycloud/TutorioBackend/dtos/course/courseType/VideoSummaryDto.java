package com.affycloud.TutorioBackend.dtos.course.courseType;

import lombok.Data;

import java.util.UUID;

@Data
public class VideoSummaryDto {
    private UUID id;
    private String title;
}
