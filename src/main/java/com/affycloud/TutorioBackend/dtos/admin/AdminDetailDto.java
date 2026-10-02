package com.affycloud.TutorioBackend.dtos.admin;

import com.affycloud.TutorioBackend.enums.Role;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class AdminDetailDto {
    private UUID id;
    private String name;
    private String email;
    private String phoneNumber;
    private String orgarnisationName;
    private String subDomain;
    private String orgarnisationType;
    private Role role;
    private Date createdAt;
    private Date updatedAt;
    private boolean isDeleted;
}
