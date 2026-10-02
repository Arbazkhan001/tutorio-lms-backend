package com.affycloud.TutorioBackend.dtos.video;

import com.affycloud.TutorioBackend.enums.ItemType;
import com.affycloud.TutorioBackend.models.Chapter;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class VideoDetailDto {

    private UUID id;

    private String title;

    private String description;

    private ItemType itemType = ItemType.VIDEO;

    private Chapter chapter;

    private Date createdAt;
}
