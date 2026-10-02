package com.affycloud.TutorioBackend.dtos.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RazorpayConfigRequest {
    @NotBlank
    private String razorpayKey;
    @NotBlank
    private String razorpaySecret;
}
