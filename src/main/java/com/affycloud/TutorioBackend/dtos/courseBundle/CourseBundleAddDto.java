package com.affycloud.TutorioBackend.dtos.courseBundle;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;

@Data
public class CourseBundleAddDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @Positive(message = "Price must be a positive number")
    private Double price;

    @NotNull(message = "Discount type is required")
    private String discountType;

    // Optional discount; if not provided, will be treated as 0.0
    @DecimalMin(value = "0.0", message = "Discount must be a non-negative number")
    private Double discount; // leave this without initialization; see below for handling in service

    private List<UUID> courseAttemptsIds;
}
