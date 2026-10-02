package com.affycloud.TutorioBackend.dtos.blog;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Data
public class BlogAddDto {

    @NotNull(message = "Title cannot be null")
    @NotEmpty(message = "Title cannot be empty")
    @Size(max = 128, message = "Title cannot exceed 128 characters")
    private String title;

    @NotNull(message = "Description cannot be null")
    @NotEmpty(message = "Description cannot be empty")
    @Size(max = 3000, message = "Description cannot exceed 3000 characters")
    private String description;

    private String author;

    private String slug;

    private MultipartFile thumbnail;

}
