package com.affycloud.TutorioBackend.dtos.course.courseType;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CourseAttemptDetailDto {
    private UUID id;
    private String attempt;
    private String plan;
    private Boolean isActive;
    private Boolean hasBatch;
    private double price;
    private String discountType;
    private double discount;
    private double finalPrice;
    private Integer expirationDays;
    private List<CourseBatchDetailDto> courseBatch;
}
