package com.affycloud.TutorioBackend.dtos.banner;

import lombok.Data;

@Data
public class BannerUpsertDto {
    private String title;
    private String description;
    private Boolean isActive;
    private Integer showDays;
}
