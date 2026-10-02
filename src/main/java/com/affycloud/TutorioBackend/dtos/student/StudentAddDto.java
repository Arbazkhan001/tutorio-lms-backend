package com.affycloud.TutorioBackend.dtos.student;

import lombok.Data;

@Data
public class StudentAddDto {
    private String name;
    private String email;
    private String phoneNumber;
    private String password;
}
