package com.affycloud.TutorioBackend.dtos.lmsPackage;



import com.affycloud.TutorioBackend.dtos.feature.FeatureDetailDto;
import com.affycloud.TutorioBackend.models.Feature;
import lombok.Data;

import java.util.Date;
import java.util.List;

import java.util.UUID;

@Data
public class PackageDetailDto {
    private UUID id;
    private String packageName;
    private String packageDescription;
    private Double price;
    private String discountType;
    private Double discountValue;
    private Double finalPrice;
    private List<FeatureDetailDto> features;  // Add back
    private Date createdAt;
    private Date updatedAt;
}
