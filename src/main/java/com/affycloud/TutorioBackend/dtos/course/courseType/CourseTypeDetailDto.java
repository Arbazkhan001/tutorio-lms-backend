package com.affycloud.TutorioBackend.dtos.course.courseType;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CourseTypeDetailDto {
    private UUID id;
    private String description;
    private String courseType;
    private List<CourseAttemptDetailDto> courseAttempts;
    private List<SubjectSummaryDto> subjects;

}
