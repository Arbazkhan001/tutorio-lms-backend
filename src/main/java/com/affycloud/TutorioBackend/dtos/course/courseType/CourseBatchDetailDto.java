package com.affycloud.TutorioBackend.dtos.course.courseType;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class CourseBatchDetailDto {
    private UUID id;
    private String batch;
    private Integer studentLimit;
    private Boolean isActive;
    private String title;
    private String description;
    private String url;
    private Date createdAt;
}
