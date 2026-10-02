package com.affycloud.TutorioBackend.dtos.login;

import lombok.Data;

@Data
public class LoginWithPhoneDto {
    private String phoneNumber;
    private String otp;
    private String token;
}
