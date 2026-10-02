package com.affycloud.TutorioBackend.dtos.ebook;

import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import lombok.Data;

import javax.validation.constraints.*;
import java.util.UUID;

@Data
public class EbookAddDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private PaymentType paymentType;

    private Double price;

    private String discountType;

    private Double discount;

    private UUID courseTypeId;

    @NotBlank(message = "Ebook file URL is required")
    private String ebookUrl;

    @NotBlank(message = "Thumbnail URL is required")
    private String thumbnailUrl;

}
