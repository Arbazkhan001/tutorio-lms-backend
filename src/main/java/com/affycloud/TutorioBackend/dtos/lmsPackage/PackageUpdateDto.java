package com.affycloud.TutorioBackend.dtos.lmsPackage;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class PackageUpdateDto {
    private String packageName;
    private String packageDescription;
    private Double price;
    private String discountType;
    private Double discountValue;
}
