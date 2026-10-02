package com.affycloud.TutorioBackend.dtos.testSeries;

import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import lombok.Data;

@Data
public class TestSeriesUpdateDto {
    private String title;
    private String description;
    private Double price;
    private String discountType;
    private Double discount;
    private PaymentType paymentType;
}


