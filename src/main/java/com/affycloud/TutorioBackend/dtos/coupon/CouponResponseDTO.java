package com.affycloud.TutorioBackend.dtos.coupon;

import com.affycloud.TutorioBackend.dtos.student.StudentDetailDto;
import lombok.Data;

import java.util.UUID;

@Data
public class CouponResponseDTO {
    private UUID id;
    private String code;
    private String description;
    private Double discount;
    private boolean isActive;
    private boolean isUsed;
    private StudentDetailDto student;
    private String createdAt;
    private String updatedAt;
}
