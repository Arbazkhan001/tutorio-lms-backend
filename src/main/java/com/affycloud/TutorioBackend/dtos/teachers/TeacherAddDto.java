package com.affycloud.TutorioBackend.dtos.teachers;

import lombok.Data;

@Data
public class TeacherAddDto {
    private String name;
    private String email;
    private String phoneNumber;
    private String username;
    private String password;
}
