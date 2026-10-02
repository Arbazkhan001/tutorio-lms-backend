package com.affycloud.TutorioBackend.dtos.course.subject;

import com.affycloud.TutorioBackend.dtos.course.subject.chapter.ChapterDetailDto;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SubjectDetailDto {
    private UUID id;
    private String name;
    private Boolean isActive;
    private List<ChapterDetailDto> chapters;
    private long chapterCount;
}
