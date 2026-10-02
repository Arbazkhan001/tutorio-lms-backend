package com.affycloud.TutorioBackend.dtos.Cart;

import lombok.Data;

import java.util.UUID;

@Data
public class AddToCartDto {
    private UUID studentId;
    private UUID itemId;
    private UUID batchId;
    private String itemType;
    private Integer quantity;
}
