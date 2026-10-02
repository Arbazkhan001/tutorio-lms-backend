package com.affycloud.TutorioBackend.dtos.testSeries.directory;

import com.affycloud.TutorioBackend.dtos.exam.ExamResponseDTO;
import com.affycloud.TutorioBackend.dtos.test.TestResponseDto;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class DirectoryResponseDto {
    private UUID id;
    private String name;
    private List<TestResponseDto> tests;
    private List<ExamResponseDTO> exams;
    private Boolean isActive;

}
