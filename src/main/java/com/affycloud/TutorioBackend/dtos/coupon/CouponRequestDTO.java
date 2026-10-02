package com.affycloud.TutorioBackend.dtos.coupon;

import lombok.Data;

import java.util.UUID;

@Data
public class CouponRequestDTO {
    private String code;
    private Double discountAmount;
    private String description;
    private UUID studentId;
}
