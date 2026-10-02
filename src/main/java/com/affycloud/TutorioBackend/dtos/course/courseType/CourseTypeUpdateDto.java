package com.affycloud.TutorioBackend.dtos.course.courseType;

import lombok.Data;

@Data
public class CourseTypeUpdateDto {
    private String description;
    private String courseType;
    private CourseAttemptDto courseAttemptDto;

}
