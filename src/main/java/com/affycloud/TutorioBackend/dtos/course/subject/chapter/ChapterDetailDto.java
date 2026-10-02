package com.affycloud.TutorioBackend.dtos.course.subject.chapter;

import lombok.Data;

import java.util.UUID;

@Data
public class ChapterDetailDto {
    private UUID id;
    private String name;
    private Boolean isActive;
    private long lectureCount;
}
