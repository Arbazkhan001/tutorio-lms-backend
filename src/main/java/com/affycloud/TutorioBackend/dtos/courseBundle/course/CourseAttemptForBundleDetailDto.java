package com.affycloud.TutorioBackend.dtos.courseBundle.course;

import com.affycloud.TutorioBackend.models.CourseAttempt;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class CourseAttemptForBundleDetailDto {

    private UUID id;
    private UUID courseId;
    private String title;
    private String description;
    private String thumbnail;
    private Date attempt;
    private double price;
    private String discountType;
    private double discount;
    private double finalPrice;

    public static CourseAttemptForBundleDetailDto fromEntity(CourseAttempt attempt) {
        CourseAttemptForBundleDetailDto dto = new CourseAttemptForBundleDetailDto();
        dto.setId(attempt.getId());
        dto.setCourseId(attempt.getCourseType().getCourse().getId());
        dto.setTitle(attempt.getCourseType().getCourse().getTitle());
        dto.setDescription(attempt.getCourseType().getDescription());
        dto.setThumbnail(attempt.getCourseType().getCourse().getThumbnail());
        dto.setAttempt(attempt.getAttempt());
        dto.setPrice(attempt.getPrice());
        dto.setDiscountType(attempt.getDiscountType());
        dto.setDiscount(attempt.getDiscount());
        dto.setFinalPrice(attempt.getFinalPrice());
        // Map the nested CourseType using its DTO
        return dto;
    }

}
