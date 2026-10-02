package com.affycloud.TutorioBackend.dtos.course.courseType;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class CourseAttemptDto {

    @DateTimeFormat(pattern = "yyyy-MM-dd")  // Define expected format
    private Date attempt;
    private Boolean hasBatch;
    private String Plan;
    private Double price;
    private String discountType;
    private Double discount;
    private Integer expirationDays;
}
