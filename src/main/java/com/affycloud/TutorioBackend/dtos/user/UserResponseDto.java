package com.affycloud.TutorioBackend.dtos.user;

import com.affycloud.TutorioBackend.enums.Role;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class UserResponseDto {
    private UUID id;
    private String name; 
    private String email; 
    private String phoneNumber; 
    private String username; 
    private Role role; 
    private Date createdAt; 
    private Date updatedAt; 
}
