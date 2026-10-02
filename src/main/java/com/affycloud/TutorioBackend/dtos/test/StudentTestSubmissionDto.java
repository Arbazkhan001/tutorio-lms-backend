package com.affycloud.TutorioBackend.dtos.test;

import lombok.Data;

import java.util.Map;
import java.util.UUID;
@Data
public class StudentTestSubmissionDto {
    private UUID testId;
    private Map<UUID, Integer> answers; 
}
