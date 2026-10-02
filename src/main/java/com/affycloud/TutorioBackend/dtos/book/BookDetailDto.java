package com.affycloud.TutorioBackend.dtos.book;

import com.affycloud.TutorioBackend.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor  // This generates the required constructor
@NoArgsConstructor   // This allows object initialization without parameters
public class BookDetailDto {
    private UUID bookId;
    private String title;
    private String description;
    private String author;
    private double price;
    private String discountType;
    private double discount;
    private double finalPrice;
    private ItemType itemType;
    private List<String> images; // Support multiple images
    private String released;
    private String language;
    private Boolean isActive;
    private boolean inCart;
    private Date createdAt;
    private Date updatedAt;
}
