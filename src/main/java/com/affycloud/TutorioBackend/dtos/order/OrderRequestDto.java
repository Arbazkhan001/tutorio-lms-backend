package com.affycloud.TutorioBackend.dtos.order;


import lombok.Data;

import java.util.UUID;

@Data
public class OrderRequestDto {
    private UUID studentId;
    private UUID addressId;
    private String couponCode;
}

