package com.affycloud.TutorioBackend.dtos.doubt;

import com.affycloud.TutorioBackend.dtos.course.courseType.CourseAttemptDetailDto;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DoubtCourseDetailDto {
    private UUID id;
    private String title;
    private String description;
    private String thumbnail;
    private String courseType;
    private List<CourseAttemptDetailDto> courseAttempts;
}
