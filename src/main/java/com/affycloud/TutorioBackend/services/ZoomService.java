package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ZoomService {
    String generateAccessToken();
    Map<String, Object> getUserDetails(String accessToken);
    Map<String, Object> createMeeting(String accessToken, String userId, Map<String, Object> meetingData);
    Map<String, Object> listMeetings(String accessToken, String userId);
    Map<String, Object> getMeetingDetails(String accessToken, String meetingId);
    void updateMeeting(String accessToken, String meetingId, Map<String, Object> updatedData);
    void deleteMeeting(String accessToken, String meetingId);
    ApiResponseWithMeta getMeetingsForStudent(UUID studentId, String status, Integer size);
    ApiResponseWithMeta getMeetingsForStudentByCourse(UUID studentId, UUID courseAttemptId);
    List<Map<String, Object>> getAllMeetingDetails(String accessToken, String userId);
    ResponseEntity<ApiResponseWithMeta> getMeetingsByCourseAttempt(UUID courseAttemptId);

}
