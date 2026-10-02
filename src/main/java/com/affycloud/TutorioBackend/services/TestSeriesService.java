package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.exam.ExamDTO;
import com.affycloud.TutorioBackend.dtos.testSeries.TestSeriesAddDto;
import com.affycloud.TutorioBackend.dtos.testSeries.TestSeriesResponseDto;
import com.affycloud.TutorioBackend.dtos.testSeries.TestSeriesResponseWithoutTestsDto;
import com.affycloud.TutorioBackend.dtos.testSeries.TestSeriesUpdateDto;
import com.affycloud.TutorioBackend.dtos.testSeries.directory.DirectoryDTO;
import com.affycloud.TutorioBackend.dtos.testSeries.directory.DirectoryResponseDto;
import com.affycloud.TutorioBackend.dtos.testSeries.exams.TestSeriesResponseWithExamAndTestDto;
import com.affycloud.TutorioBackend.dtos.testSeries.test.TestAddInTestSeriesDto;
import com.affycloud.TutorioBackend.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TestSeriesService {
    Map<String, Object> createTestSeries(TestSeriesAddDto request, User user)throws IOException;
    TestSeriesResponseDto getTestSeriesById(UUID id,UUID tenantId);
    TestSeriesResponseDto getTestSeriesById(UUID id,UUID tenantId, UUID studentId);
    Map<String, Object> addTestToTestSeries(UUID testSeriesId, TestAddInTestSeriesDto request);
    Map<String, Object> addExamToTestSeries(UUID testSeriesId, ExamDTO request);
    Map<String, Object> addDirectoryInTestSeries(UUID testSeriesId, DirectoryDTO request);
    Map<String, Object> addTestInDirectory(UUID directoryId, TestAddInTestSeriesDto request);
    Map<String, Object> addExamInDirectory(UUID directoryId, ExamDTO request);
    Page<TestSeriesResponseDto> getAllTestSeries(Pageable pageable, UUID tenantId, String paymentType);
    Page<Map<String, Object>> getAllTestSeriesByStudent(Pageable pageable, UUID studentId, UUID tenantId, String paymentType);
    Page<TestSeriesResponseWithoutTestsDto> getAllTestSeriesWithTestCount(Pageable pageable,UUID tenantId);
    DirectoryResponseDto getDirectoryDetailsByDirectoryId(UUID directoryId);
    List<TestSeriesResponseDto> getTestSeriesByUserId(UUID userId); 
    void deleteTestSeries(UUID id);
    Map<String, Object> updateTestSeries(UUID id, TestSeriesUpdateDto request, MultipartFile thumbnail) throws IOException;
    TestSeriesResponseWithExamAndTestDto getTestSeriesByIdWithExamsAndTest(UUID id, UUID studentId);
   
}
