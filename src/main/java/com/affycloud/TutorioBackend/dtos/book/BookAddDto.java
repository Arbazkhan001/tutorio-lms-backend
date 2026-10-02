package com.affycloud.TutorioBackend.dtos.book;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import javax.validation.constraints.Positive;
import java.util.List;

@Data
public class BookAddDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String author;

    @Positive(message = "Price must be a positive number")
    private Double price;

    @NotNull(message = "Discount type is required")
    private String discountType;

    @DecimalMin(value = "0.0", message = "Discount must be a non-negative number")
    private Double discount;

    @NotEmpty(message = "At least one image is required")
    private List<MultipartFile> images; // Support multiple images

    private String released;

    private String language;
}
