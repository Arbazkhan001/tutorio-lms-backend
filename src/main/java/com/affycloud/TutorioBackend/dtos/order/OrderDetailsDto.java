package com.affycloud.TutorioBackend.dtos.order;


import com.affycloud.TutorioBackend.dtos.coupon.CouponDetailDTO;
import com.affycloud.TutorioBackend.dtos.order.orderItems.OrderItemResponseDto;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class OrderDetailsDto {
    private UUID orderId;
    private UUID studentId;
    private String razorPayId;
    private String randomId;
    private String paymentStatus;
    private String status;
    private CouponDetailDTO coupon;
    private Double amount;
    private Double discountAmount;
    private Double totalAmount;
    private Date createdAt;
    private List<OrderItemResponseDto> orderItems;
}

