package com.affycloud.TutorioBackend.dtos.superAdmin;

import lombok.Data;

@Data
public class SuperAdminAddDto {
    private String name;
    private String email;
    private String phoneNumber;
    private String username;
    private String password;
}
