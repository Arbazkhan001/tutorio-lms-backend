package com.affycloud.TutorioBackend.dtos.login;

import lombok.Data;

@Data
public class LoginVerifyDto {
    private String email;
    private String otp;
}
