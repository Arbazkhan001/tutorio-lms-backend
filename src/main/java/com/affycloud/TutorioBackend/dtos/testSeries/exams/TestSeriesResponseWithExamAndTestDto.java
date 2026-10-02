package com.affycloud.TutorioBackend.dtos.testSeries.exams;

import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.dtos.testSeries.directory.TestSeriesDirectoriesDto;
import com.affycloud.TutorioBackend.dtos.user.UserResponseDto;
import com.affycloud.TutorioBackend.enums.ContentType;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class TestSeriesResponseWithExamAndTestDto {
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
    // Unified list for both tests and exams
    private List<TestSeriesUnifiedItemDto> items;
    private List<TestSeriesDirectoriesDto> directories;
}
