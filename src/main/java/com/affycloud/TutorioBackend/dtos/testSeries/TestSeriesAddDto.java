package com.affycloud.TutorioBackend.dtos.testSeries;


import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.enums.ContentType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class TestSeriesAddDto {
    private String title; 
    private String description;
    private PaymentType paymentType;
    private Double price= 0.0;
    private String discountType;
    private Double discount;
    private MultipartFile thumbnail; // New field for thumbnail file upload
    private ContentType contentType; // New field for content type
}
