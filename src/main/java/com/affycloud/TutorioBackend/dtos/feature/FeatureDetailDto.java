package com.affycloud.TutorioBackend.dtos.feature;


import lombok.Data;
import java.util.Date;
import java.util.UUID;

@Data
public class FeatureDetailDto {
    private UUID id;
    private String name;
    private String key;
    private String description;
    private Date createdAt;
    private Date updatedAt;

    private boolean inPackage;
}
