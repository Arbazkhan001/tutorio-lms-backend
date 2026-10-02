package com.affycloud.TutorioBackend.dtos.forgetPassword;



import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgetPasswordDto {

    @NotBlank(message = "Old Password is mandatory")
    private String oldPassword;

    @NotBlank(message = "New Password is mandatory")
    private String newPassword;
}
