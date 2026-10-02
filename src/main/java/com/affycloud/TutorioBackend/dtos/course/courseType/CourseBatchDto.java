package com.affycloud.TutorioBackend.dtos.course.courseType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourseBatchDto {
    @NotBlank(message = "Batch name is required")
    private String batch;

    @NotNull(message = "Student limit is required")
    @Min(value = 1, message = "Student limit must be greater than 0")
    private Integer studentLimit;
}
