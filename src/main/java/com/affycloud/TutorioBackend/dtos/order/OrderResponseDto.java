package com.affycloud.TutorioBackend.dtos.order;


import com.affycloud.TutorioBackend.enums.ItemType;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class OrderResponseDto {
    private UUID orderId;
    private UUID studentId;
    private String studentName;
    private String status;
    private Date createdAt;
    private List<OrderItemResponseDto> orderItems;

    @Data
    public static class OrderItemResponseDto {
        private UUID orderItemId;
        private ItemType itemType;
        private UUID itemId;
        private String itemName;
        private Integer quantity;
    }
}

