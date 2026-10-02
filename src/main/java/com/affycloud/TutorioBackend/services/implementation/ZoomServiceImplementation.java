//package com.affycloud.TutorioBackend.services.implementation;
//
//import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
//import com.affycloud.TutorioBackend.models.*;
//import com.affycloud.TutorioBackend.repositories.CourseAttemptRepository;
//import com.affycloud.TutorioBackend.repositories.EnrollmentRepository;
//import com.affycloud.TutorioBackend.repositories.MeetingRepository;
//import com.affycloud.TutorioBackend.services.ZoomService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.*;
//import org.springframework.http.converter.FormHttpMessageConverter;
//import org.springframework.stereotype.Service;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class ZoomServiceImplementation implements ZoomService {
//
//    @Value("${zoom.client.id}")
//    private String clientId;
//
//    @Value("${zoom.client.secret}")
//    private String clientSecret;
//
//    @Value("${zoom.account.id}")
//    private String accountId;
//
//    private final RestTemplate restTemplate;
//
//    public ZoomServiceImplementation() {
//        this.restTemplate = new RestTemplate();
//        this.restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
//    }
//
//        @Autowired
//    private EnrollmentRepository enrollmentRepository;
//
//        @Autowired
//    private MeetingRepository meetingRepository;
//        @Autowired
//    private CourseAttemptRepository courseAttemptRepository;
//
//
//    // Generate access token
//    public String generateAccessToken() {
//        String authString = clientId + ":" + clientSecret;
//        String encodedAuth = Base64.getEncoder().encodeToString(authString.getBytes());
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Basic " + encodedAuth);
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
//        requestBody.add("grant_type", "account_credentials");
//        requestBody.add("account_id", accountId);
//
//        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);
//
//        ResponseEntity<Map> response = restTemplate.postForEntity(
//                "https://zoom.us/oauth/token",
//                request,
//                Map.class
//        );
//
//        return (String) response.getBody().get("access_token");
//    }
//
//    // Get user details
//    public Map<String, Object> getUserDetails(String accessToken) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//        HttpEntity<String> request = new HttpEntity<>(headers);
//
//        return restTemplate.exchange(
//                "https://api.zoom.us/v2/users/me",
//                HttpMethod.GET,
//                request,
//                Map.class
//        ).getBody();
//    }
//
//    // Create a meeting
//    public Map<String, Object> createMeeting(String accessToken, String userId, Map<String, Object> meetingData) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(meetingData, headers);
//
//        return restTemplate.postForObject(
//                "https://api.zoom.us/v2/users/" + userId + "/meetings",
//                request,
//                Map.class
//        );
//    }
//
//    // List all meetings for a user
//    public Map<String, Object> listMeetings(String accessToken, String userId) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//        HttpEntity<String> request = new HttpEntity<>(headers);
//
//        return restTemplate.exchange(
//                "https://api.zoom.us/v2/users/" + userId + "/meetings",
//                HttpMethod.GET,
//                request,
//                Map.class
//        ).getBody();
//    }
//
//    // Get meeting details
//    public Map<String, Object> getMeetingDetails(String accessToken, String meetingId) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//        HttpEntity<String> request = new HttpEntity<>(headers);
//
//        return restTemplate.exchange(
//                "https://api.zoom.us/v2/meetings/" + meetingId,
//                HttpMethod.GET,
//                request,
//                Map.class
//        ).getBody();
//    }
//
//    // Update a meeting
//    public void updateMeeting(String accessToken, String meetingId, Map<String, Object> updatedData) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        HttpEntity<Map<String, Object>> request = new HttpEntity<>(updatedData, headers);
//
//        restTemplate.patchForObject(
//                "https://api.zoom.us/v2/meetings/" + meetingId,
//                request,
//                Void.class
//        );
//    }
//
//    // Delete a meeting
//    public void deleteMeeting(String accessToken, String meetingId) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//        HttpEntity<String> request = new HttpEntity<>(headers);
//
//        restTemplate.exchange(
//                "https://api.zoom.us/v2/meetings/" + meetingId,
//                HttpMethod.DELETE,
//                request,
//                Void.class
//        );
//    }
//
//    public ApiResponseWithMeta getMeetingsForStudent(UUID studentId, String status, Integer size) {
//        Map<String, Object> meta = new HashMap<>();
//        LocalDateTime now = LocalDateTime.now();
//
//        // Get student's enrollments and course attempts
//        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
//        if (enrollments.isEmpty()) {
//            meta.put("meetings", Collections.emptyList());
//            return new ApiResponseWithMeta("No enrollments found for the student.", true, meta);
//        }
//
//        List<UUID> enrolledCourseAttemptIds = enrollments.stream()
//                .map(Enrollment::getCourseAttempt)
//                .filter(Objects::nonNull)
//                .map(CourseAttempt::getId)
//                .collect(Collectors.toList());
//
//        // Get all meetings for these course attempts
//        List<Meeting> meetings = meetingRepository.findByCourseAttemptIdIn(enrolledCourseAttemptIds);
//
//        // Categorize meetings
//        List<Map<String, Object>> upcomingMeetings = new ArrayList<>();
//        List<Map<String, Object>> completedMeetings = new ArrayList<>();
//        List<Map<String, Object>> canceledMeetings = new ArrayList<>();
//
//        for (Meeting meeting : meetings) {
//            Map<String, Object> meetingMap = new HashMap<>();
//            meetingMap.put("meetingId", meeting.getMeetingId());
//            meetingMap.put("meetingUrl", meeting.getMeetingUrl());
//            meetingMap.put("topic", meeting.getTopic());
//            meetingMap.put("startTime", meeting.getStartTime());
//            meetingMap.put("duration", meeting.getDuration());
//            meetingMap.put("agenda", meeting.getAgenda());
//            meetingMap.put("courseAttemptId", meeting.getCourseAttempt().getId());
//
//            // Add course details
//            CourseAttempt courseAttempt = meeting.getCourseAttempt();
//            CourseType courseType = courseAttempt.getCourseType();
//            Course course = courseType.getCourse();
//
//            Map<String, Object> courseDetails = new HashMap<>();
//            courseDetails.put("courseId", course.getId());
//            courseDetails.put("courseTitle", course.getTitle());
//            courseDetails.put("courseDescription", courseType.getDescription());
//            courseDetails.put("courseAttempt", courseAttempt.getAttempt());
//
//            meetingMap.put("courseDetails", courseDetails);
//
//            if (meeting.isCanceled()) {
//                canceledMeetings.add(meetingMap);
//            } else {
//                LocalDateTime startTime = LocalDateTime.parse(meeting.getStartTime().substring(0, 19));
//                LocalDateTime endTime = startTime.plusMinutes(meeting.getDuration());
//
//                if (startTime.isAfter(now)) {
//                    upcomingMeetings.add(meetingMap);
//                } else {
//                    completedMeetings.add(meetingMap);
//                }
//            }
//        }
//
//        // Filter meetings based on status
//        List<Map<String, Object>> filteredMeetings;
//        if (status != null) {
//            switch (status.toLowerCase()) {
//                case "upcoming":
//                    filteredMeetings = upcomingMeetings;
//                    break;
//                case "completed":
//                    filteredMeetings = completedMeetings;
//                    break;
//                case "canceled":
//                    filteredMeetings = canceledMeetings;
//                    break;
//                default:
//                    filteredMeetings = new ArrayList<>();
//                    filteredMeetings.addAll(upcomingMeetings);
//                    filteredMeetings.addAll(completedMeetings);
//                    filteredMeetings.addAll(canceledMeetings);
//            }
//        } else {
//            filteredMeetings = new ArrayList<>();
//            filteredMeetings.addAll(upcomingMeetings);
//            filteredMeetings.addAll(completedMeetings);
//            filteredMeetings.addAll(canceledMeetings);
//        }
//
//        // Sort upcoming meetings by startTime
//        upcomingMeetings.sort(Comparator.comparing(m -> LocalDateTime.parse(m.get("startTime").toString().substring(0, 19))));
//
//        // Apply size limit if provided
//        if (size != null && size > 0 && size < filteredMeetings.size()) {
//            filteredMeetings = filteredMeetings.subList(0, size);
//        }
//
//        meta.put("meetings", filteredMeetings);
//        meta.put("counts", Map.of(
//                "total", meetings.size(),
//                "upcoming", upcomingMeetings.size(),
//                "completed", completedMeetings.size(),
//                "canceled", canceledMeetings.size()
//        ));
//
//        return new ApiResponseWithMeta("Meetings retrieved successfully.", false, meta);
//    }
//
//
//    public ApiResponseWithMeta getMeetingsForStudentByCourse(UUID studentId, UUID courseAttemptId) {
//        Map<String, Object> meta = new HashMap<>();
//
//        boolean isEnrolled = enrollmentRepository.existsByStudentIdAndCourseAttemptId(studentId, courseAttemptId);
//        if (!isEnrolled) {
//            meta.put("meetings", Collections.emptyList());
//            return new ApiResponseWithMeta("Student is not enrolled in this course.", true, meta);
//        }
//
//        List<Meeting> meetings = meetingRepository.findByCourseAttemptId(courseAttemptId);
//        meta.put("meetings", meetings);
//
//        return new ApiResponseWithMeta("Meetings retrieved successfully.", false, meta);
//    }
//
//public List<Map<String, Object>> getAllMeetingDetails(String accessToken, String userId) {
//    HttpHeaders headers = new HttpHeaders();
//    headers.set("Authorization", "Bearer " + accessToken);
//    HttpEntity<String> request = new HttpEntity<>(headers);
//
//    // Get the list of meetings
//    ResponseEntity<Map> response = restTemplate.exchange(
//            "https://api.zoom.us/v2/users/" + userId + "/meetings",
//            HttpMethod.GET,
//            request,
//            Map.class
//    );
//
//    List<Map<String, Object>> meetings = (List<Map<String, Object>>) response.getBody().get("meetings");
//    List<Map<String, Object>> fullMeetings = new ArrayList<>();
//
//    // Fetch details for each meeting
//    for (Map<String, Object> meeting : meetings) {
//        Number meetingIdNumber = (Number) meeting.get("id");
//        String meetingId = String.valueOf(meetingIdNumber);
//        ResponseEntity<Map> meetingResponse = restTemplate.exchange(
//                "https://api.zoom.us/v2/meetings/" + meetingId,
//                HttpMethod.GET,
//                request,
//                Map.class
//        );
//
//        fullMeetings.add(meetingResponse.getBody());
//    }
//
//    return fullMeetings;
//}
//
//
//
//
//
//public ResponseEntity<ApiResponseWithMeta> getMeetingsByCourseAttempt(UUID courseAttemptId) {
//    Optional<CourseAttempt> courseAttemptOpt = courseAttemptRepository.findById(courseAttemptId);
//    if (courseAttemptOpt.isEmpty()) {
//        return ResponseEntity.badRequest().body(new ApiResponseWithMeta(
//                "Invalid Course Attempt ID",
//                true,
//                Collections.emptyMap()
//        ));
//    }
//
//    List<Meeting> meetings = meetingRepository.findByCourseAttempt(courseAttemptOpt.get());
//
//    if (meetings.isEmpty()) {
//        return ResponseEntity.ok(new ApiResponseWithMeta(
//                "No meetings found for this course attempt.",
//                false,
//                Map.of("meetings", new ArrayList<>())
//        ));
//    }
//
//    List<Map<String, Object>> meetingList = meetings.stream().map(meeting -> {
//        Map<String, Object> map = new HashMap<>();
//        map.put("meetingId", (Object) meeting.getMeetingId());
//        map.put("topic", (Object) meeting.getTopic());
//        map.put("startTime", (Object) meeting.getStartTime());
//        map.put("duration", (Object) meeting.getDuration());
//        map.put("agenda", (Object) meeting.getAgenda());
//        map.put("meetingUrl", (Object) meeting.getMeetingUrl());
//        map.put("startUrl", (Object) meeting.getStartUrl());
//        return map;
//    }).collect(Collectors.toList());
//
//    return ResponseEntity.ok(new ApiResponseWithMeta(
//            "Meetings retrieved successfully.",
//            false,
//            Map.of("meetings", meetingList)
//    ));
//}
//
//}
