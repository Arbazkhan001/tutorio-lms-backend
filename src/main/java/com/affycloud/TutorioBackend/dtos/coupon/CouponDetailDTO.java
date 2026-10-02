package com.affycloud.TutorioBackend.dtos.coupon;

import lombok.Data;

@Data
public class CouponDetailDTO {
    private String code;
    private String description;
    private Double discount;
}
