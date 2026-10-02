package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.question.QuestionResponseDto;
import com.affycloud.TutorioBackend.dtos.question.QuestionUpdateDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.dtos.test.TestAddDto;
import com.affycloud.TutorioBackend.dtos.test.TestUpdateDto;
import com.affycloud.TutorioBackend.models.Test;

import java.util.List;
import java.util.UUID;

public interface TestService {
            ApiResponse createTest(TestAddDto request, UUID userId);
            List<Test> getAllTests();
            ApiResponse updateTest(UUID testId, TestUpdateDto request);
            ApiResponseWithMeta getTestsByCourseType(UUID courseTypeId,UUID studentId);
            ApiResponseWithMeta getTestById(UUID testId);
            ApiResponse updateQuestion(UUID questionId, QuestionUpdateDto request);
            QuestionResponseDto getQuestionById(UUID questionId);
        }
