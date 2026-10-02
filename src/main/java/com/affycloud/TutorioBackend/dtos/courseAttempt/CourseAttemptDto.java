package com.affycloud.TutorioBackend.dtos.courseAttempt;

import lombok.Data;

import java.util.UUID;

@Data
public class CourseAttemptDto {
    private UUID courseAttemptId;
    private String attempt;
    private double price;
    private String discountType;
    private double discount;
    private double finalPrice;
}
