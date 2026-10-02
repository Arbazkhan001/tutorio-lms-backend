package com.affycloud.TutorioBackend.dtos.courseBundle;

import com.affycloud.TutorioBackend.models.CourseBundle;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class CourseBundleSummaryDetailDto {
    private UUID id;
    private String title;
    private String description;
    private double price;
    private String discountType;
    private double discount;
    private double finalPrice;
    private String thumbnail;
    private int courseCount; // Number of course attempts
    private Date createdAt;


    public static CourseBundleSummaryDetailDto fromEntity(CourseBundle bundle) {
        CourseBundleSummaryDetailDto dto = new CourseBundleSummaryDetailDto();
        dto.setId(bundle.getId());
        dto.setTitle(bundle.getTitle());
        dto.setDescription(bundle.getDescription());
        dto.setThumbnail(bundle.getThumbnail());
        dto.setPrice(bundle.getPrice());
        dto.setDiscountType(bundle.getDiscountType());
        dto.setDiscount(bundle.getDiscount());
        dto.setFinalPrice(bundle.getFinalPrice());
        dto.setCreatedAt(bundle.getCreatedAt());
        // Map each CourseAttempt to CourseAttemptDetailDto
        dto.setCourseCount(bundle.getCourseAttempts() != null ? bundle.getCourseAttempts().size() : 0);

        return dto;
    }
}
