package com.affycloud.TutorioBackend.dtos.testSeries;


import com.affycloud.TutorioBackend.dtos.exam.ExamResponseDTO;
import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.dtos.test.TestResponseDto;
import com.affycloud.TutorioBackend.dtos.testSeries.directory.DirectoryResponseDto;
import com.affycloud.TutorioBackend.dtos.user.UserResponseDto;
import com.affycloud.TutorioBackend.enums.ContentType;
import com.affycloud.TutorioBackend.enums.ItemType;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class TestSeriesResponseDto {
    private UUID id;
    private String title;
    private String description;
    private PaymentType paymentType;
    private Double price;
    private String discountType;
    private Double discount;
    private Double finalPrice;
    private String thumbnail;
    private UserResponseDto createdBy;
    private ContentType contentType;
    private ItemType itemType;
    private List<DirectoryResponseDto> directories;
    private List<TestResponseDto> tests;
    private List<ExamResponseDTO> exams;
    private boolean inMyLearning;
    private boolean inCart;
    private Date createdAt;
    private Date updatedAt;
}
