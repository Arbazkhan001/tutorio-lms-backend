package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.dtos.test.StudentTestSubmissionDto;

import java.util.Map;
import java.util.UUID;

public interface ResultService {

     ApiResponseWithMeta submitTest(StudentTestSubmissionDto submission, UUID studentId);
     Map<String, Object> getTestResult(UUID testId, UUID studentId);

}
