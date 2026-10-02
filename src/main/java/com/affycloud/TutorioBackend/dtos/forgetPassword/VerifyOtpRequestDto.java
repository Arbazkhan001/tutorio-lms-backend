package com.affycloud.TutorioBackend.dtos.forgetPassword;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequestDto {
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "OTP is mandatory")
    private String otp;

    @NotBlank(message = "New Password is mandatory")
    private String newPassword;
}
