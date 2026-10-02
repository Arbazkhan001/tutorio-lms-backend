package com.affycloud.TutorioBackend.dtos.Cart;

import com.affycloud.TutorioBackend.dtos.course.courseType.CourseAttemptDto;
import lombok.Data;

import java.util.UUID;


@Data
public  class CartItemDto {
    private UUID cartId;
    private CourseAttemptDto courseAttempt;

}
