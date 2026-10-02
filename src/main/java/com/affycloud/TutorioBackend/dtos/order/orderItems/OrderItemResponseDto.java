package com.affycloud.TutorioBackend.dtos.order.orderItems;

import com.affycloud.TutorioBackend.enums.ItemType;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class OrderItemResponseDto {
    private UUID orderItemId;
    private ItemType itemType;
    private UUID itemId;
    private UUID courseBatchId;
    private String title;
    private String courseType;
    private Date courseAttempt;
    private Double price;
    private String discountType;
    private Double discount;
    private Double finalPrice;
    private Double totalPrice;
    private Integer quantity;
    private Date createdAt;
}