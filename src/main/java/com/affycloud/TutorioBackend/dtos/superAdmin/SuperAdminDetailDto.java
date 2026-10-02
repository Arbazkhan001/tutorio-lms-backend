package com.affycloud.TutorioBackend.dtos.superAdmin;

import lombok.Data;

import java.util.UUID;

@Data
public class SuperAdminDetailDto {
    private UUID id;
    private String name;
    private String email;
    private String phoneNumber;
    private String username;
    private String role;
}
