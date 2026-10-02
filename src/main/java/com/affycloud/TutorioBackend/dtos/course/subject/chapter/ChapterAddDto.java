package com.affycloud.TutorioBackend.dtos.course.subject.chapter;

import lombok.Data;

import java.util.UUID;

@Data
public class ChapterAddDto {
    private UUID subjectId;
    private String name;
}
