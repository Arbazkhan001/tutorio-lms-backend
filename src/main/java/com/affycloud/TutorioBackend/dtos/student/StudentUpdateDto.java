package com.affycloud.TutorioBackend.dtos.student;

import lombok.Data;

import java.util.Date;

@Data
public class StudentUpdateDto {
    private String name;
    private String phoneNumber;
    private Date dateOfBirth;
    private String countryCode;
}
