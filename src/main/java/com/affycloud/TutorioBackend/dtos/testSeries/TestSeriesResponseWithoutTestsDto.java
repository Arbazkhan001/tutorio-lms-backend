package com.affycloud.TutorioBackend.dtos.testSeries;


import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.dtos.user.UserResponseDto;
import com.affycloud.TutorioBackend.enums.ContentType;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class TestSeriesResponseWithoutTestsDto {
    private UUID id; 
    private String title; 
    private String description;
    private PaymentType paymentType;
    private Double price;
    private String discountType;
    private Double discount;
    private Double finalPrice;
    private String thumbnail;
    private ContentType contentType;
    private UserResponseDto createdBy;
    private Date createdAt; 
    private Date updatedAt;
    private Long testCount;
    private Long examCount;
}
