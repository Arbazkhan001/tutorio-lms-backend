package com.affycloud.TutorioBackend.dtos.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

import java.util.UUID;

@Data
@Setter
@AllArgsConstructor
public class TestResultDto {

    private UUID id;
    private String title;
    private String description;
    private boolean attempted;
    private Integer score;

}
