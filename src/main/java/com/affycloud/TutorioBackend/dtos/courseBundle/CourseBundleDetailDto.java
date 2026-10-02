package com.affycloud.TutorioBackend.dtos.courseBundle;

import com.affycloud.TutorioBackend.dtos.courseBundle.course.CourseAttemptForBundleDetailDto;
import com.affycloud.TutorioBackend.models.CourseBundle;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class CourseBundleDetailDto {
    private UUID id;
    private String title;
    private String description;
    private double price;
    private String discountType;
    private double discount;
    private double finalPrice;
    private String thumbnail;
    private boolean inCart;
    // Replace the raw CourseAttempt with the new DTO
    private List<CourseAttemptForBundleDetailDto> courseAttempts;
    private Date createdAt;


    public static CourseBundleDetailDto fromEntity(CourseBundle bundle) {
        CourseBundleDetailDto dto = new CourseBundleDetailDto();
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
        dto.setCourseAttempts(
                bundle.getCourseAttempts()
                        .stream()
                        .filter(attempt -> attempt.getIsActive() != null && attempt.getIsActive())
                        .map(CourseAttemptForBundleDetailDto::fromEntity)
                        .collect(Collectors.toList())
        );
        return dto;
    }
}
