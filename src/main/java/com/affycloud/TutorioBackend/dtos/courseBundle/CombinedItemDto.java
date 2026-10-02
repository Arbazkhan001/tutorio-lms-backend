package com.affycloud.TutorioBackend.dtos.courseBundle;


import com.affycloud.TutorioBackend.dtos.course.courseType.CourseTypeDetailDto;
import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.enums.ItemType;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class CombinedItemDto {
    private UUID id;
    private String title;
    private PaymentType paymentType;
    private String description;
    private double price;
    private List<CourseTypeDetailDto> courseTypes;
    private String discountType;
    private double discount;
    private double finalPrice;
    private String thumbnail;
    private Date createdAt;
    private ItemType itemType;
    private boolean inCart;
    private boolean inMyLearning;

}
