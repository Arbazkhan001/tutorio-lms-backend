package com.affycloud.TutorioBackend.dtos.admin;

import lombok.Data;

@Data
public class AdminAddDto {
    private String name;
    private String email;
    private String phoneNumber;
    private String username;
    private String password;
}
