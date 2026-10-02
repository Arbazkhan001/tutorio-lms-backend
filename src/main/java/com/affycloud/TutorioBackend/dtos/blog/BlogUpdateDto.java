package com.affycloud.TutorioBackend.dtos.blog;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BlogUpdateDto {

    private String title;

    private String description;

    private String author;

    private MultipartFile thumbnail;
}
