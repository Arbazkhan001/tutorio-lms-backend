package com.affycloud.TutorioBackend.dtos.ebook;

import lombok.Data;

@Data
public class EbookUpdateDto {

    private String title;

    private String description;

    private Double price;

    private String discountType;

    private Double discount;

    private String ebookUrl;

    private String thumbnailUrl;

}
