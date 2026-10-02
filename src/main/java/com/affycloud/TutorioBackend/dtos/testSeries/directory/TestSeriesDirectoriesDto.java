package com.affycloud.TutorioBackend.dtos.testSeries.directory;

import com.affycloud.TutorioBackend.dtos.testSeries.exams.TestSeriesUnifiedItemDto;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class TestSeriesDirectoriesDto {
    private UUID id;
    private String name;
    private List<TestSeriesUnifiedItemDto> items;
}
