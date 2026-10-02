package com.affycloud.TutorioBackend.dtos.course.courseType;

import lombok.Data;

@Data
public class CourseTypeDto {
    private String description;
    private String courseType;
    private CourseAttemptDto courseAttemptDto;


}
