package com.affycloud.TutorioBackend.dtos.course;

import lombok.Data;

@Data
public class CourseStatusUpdateDto {

    private Boolean isActive;
    private Boolean isPopular;
    private Boolean onBanner;
}
