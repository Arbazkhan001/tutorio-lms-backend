package com.affycloud.TutorioBackend.dtos.blog;


import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class BlogDetailDto {
    private UUID id;
    private String title;
    private String description;
    private String author;
    private String slug;
    private String thumbnail;
    private Date createdAt;
    private Date updatedAt;
}
