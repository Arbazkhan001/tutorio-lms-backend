package com.affycloud.TutorioBackend.dtos.courseBundle;

import lombok.Data;

@Data
public class CourseBundleUpdateDto {
    private String title;
    private String description;
    private Double price;
    private String discountType;
    private Double discount;
}
