package com.affycloud.TutorioBackend.dtos.course;

import com.affycloud.TutorioBackend.dtos.course.courseType.CourseTypeDto;
import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import lombok.Data;

@Data
public class CourseAddDto {
    private String title;
    private String collection;
    private String category;
    private String subject;
    private PaymentType paymentType;
    private CourseTypeDto courseTypeDto;

}
