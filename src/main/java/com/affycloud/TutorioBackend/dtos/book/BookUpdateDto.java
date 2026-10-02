package com.affycloud.TutorioBackend.dtos.book;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.util.List;

@Data
public class BookUpdateDto {


    private String title;
    private String description;
    private String author;


    @Positive(message = "Price must be a positive number")
    private Double price;

    private String discountType;

    @DecimalMin(value = "0.0", message = "Discount must be a non-negative number")
    private Double discount;

    private List<MultipartFile> images; // Support multiple images
    private String released;
    private String language;
}
