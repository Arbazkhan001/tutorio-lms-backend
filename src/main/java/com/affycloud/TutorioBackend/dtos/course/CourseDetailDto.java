package com.affycloud.TutorioBackend.dtos.course;

import com.affycloud.TutorioBackend.dtos.course.courseType.CourseTypeDetailDto;
import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.enums.ItemType;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class CourseDetailDto {
    private UUID id;
    private String title;
    private String Collection;
    private String Category;
    private String Subject;
    private PaymentType paymentType;
    private List<CourseTypeDetailDto> courseTypes;
    private String thumbnail;
    private ItemType itemType;
    private Boolean isActive;
    private Boolean isPopular;
    private Boolean onBanner;
    private Date createdAt;
    private Date updatedAt;
    private boolean inMyLearning;
    private boolean inCart;
    private UUID attemptInTheCart;
}
