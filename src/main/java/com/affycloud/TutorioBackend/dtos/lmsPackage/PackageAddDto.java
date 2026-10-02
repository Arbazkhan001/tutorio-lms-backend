package com.affycloud.TutorioBackend.dtos.lmsPackage;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PackageAddDto {
    private String packageName;
    private String packageDescription;
    private Double price;
    private String discountType;   // FLAT or PERCENTAGE
    private Double discountValue;
    private List<UUID> featureIds;   // selected features
}
