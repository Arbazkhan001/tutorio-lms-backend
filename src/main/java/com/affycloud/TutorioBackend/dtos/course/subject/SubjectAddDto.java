package com.affycloud.TutorioBackend.dtos.course.subject;

import com.affycloud.TutorioBackend.dtos.course.subject.chapter.ChapterAddDto;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SubjectAddDto {
    private UUID courseTypeId;
    private String name;
    private List<ChapterAddDto> chapters;
}
