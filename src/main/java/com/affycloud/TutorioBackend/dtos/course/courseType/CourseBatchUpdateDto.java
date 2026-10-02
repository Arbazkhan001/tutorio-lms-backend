package com.affycloud.TutorioBackend.dtos.course.courseType;

import lombok.Data;

@Data
public class CourseBatchUpdateDto {
    private String batch;
    private Integer studentLimit;
    private String title;
    private String description;
    private String url;
}
