package com.affycloud.TutorioBackend.dtos.video;

import com.affycloud.TutorioBackend.enums.ItemType;
import com.affycloud.TutorioBackend.models.Chapter;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class VideoRequestDto {

    private String title;
    private String description;
    private Double countLimit;
    private UUID chapterId;
    private String videoUrl;
}
