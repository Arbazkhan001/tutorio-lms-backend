package com.affycloud.TutorioBackend.dtos.student;

import com.affycloud.TutorioBackend.enums.Role;
import lombok.Data;
import java.util.Date;
import java.util.UUID;

@Data
public class StudentDetailDto {
    private UUID studentId;
    private String name;
    private String email;
    private String phoneNumber;
    private Role role;
    private Long age;
    private Date dateOfBirth;
    private UUID tenantId;
    private String countryCode;
    private String batchName;
    private String courseName;
    private String courseType;
    private String planName;
}
