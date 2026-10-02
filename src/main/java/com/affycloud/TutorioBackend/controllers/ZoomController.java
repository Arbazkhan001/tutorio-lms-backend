//package com.affycloud.TutorioBackend.controllers;
//
//import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
//import com.affycloud.TutorioBackend.dtos.zoom.CreateMeetingDTO;
//import com.affycloud.TutorioBackend.models.CourseAttempt;
//import com.affycloud.TutorioBackend.models.Enrollment;
//import com.affycloud.TutorioBackend.models.Meeting;
//import com.affycloud.TutorioBackend.repositories.CourseAttemptRepository;
//import com.affycloud.TutorioBackend.repositories.EnrollmentRepository;
//import com.affycloud.TutorioBackend.repositories.MeetingRepository;
//import com.affycloud.TutorioBackend.services.ZoomService;
//import io.swagger.v3.oas.annotations.Operation;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.*;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//
//@RestController
//@RequestMapping("/api/zoom")
//public class ZoomController {
//
//    @Autowired
//    private ZoomService zoomService;
//
//    @Autowired
//    private MeetingRepository meetingRepository;
//
//    @Autowired
//    private EnrollmentRepository enrollmentRepository;
//
//    @Autowired
//    private CourseAttemptRepository courseAttemptRepository;
//
//    @Autowired
//    private RestTemplate restTemplate;
//
//    @Operation(summary = "Generate Zoom Access Token")
//    @GetMapping("/generate-token")
//    public Map<String, String> generateToken() {
//        String accessToken = zoomService.generateAccessToken();
//        return Map.of("accessToken", accessToken);
//    }
//
//    @Operation(summary = "Get User Details")
//    @GetMapping("/user-details")
//    public Map<String, Object> getUserDetails() {
//        String accessToken = zoomService.generateAccessToken();
//        return zoomService.getUserDetails(accessToken);
//    }
//
//    @Operation(
//        summary = "Create a Zoom Meeting",
//        description = """
//            **Meeting Types:**
//            - **Instant Meeting** (1) → A meeting that starts immediately.
//            - **Scheduled Meeting** (2) → A meeting scheduled for a future time.
//            - **Recurring Meeting (No Fixed Time)** (3) → A recurring meeting without a fixed schedule.
//            - **Recurring Meeting (Fixed Time)** (8) → A recurring meeting with a scheduled time.
//        """
//    )
//    @PostMapping("/create-meeting")
//    public ResponseEntity<Map<String, Object>> createMeeting(
//            @RequestBody @Valid CreateMeetingDTO meetingDTO,
//            @RequestParam("courseAttemptId") UUID courseAttemptId) {
//
//        Map<String, Object> response = new HashMap<>();
//        // Validate start time format
//        if (meetingDTO.getStartTime() == null || meetingDTO.getStartTime().isEmpty()) {
//            response.put("message", "Start time is required.");
//            response.put("error", true);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        }
//        // Validate courseAttemptId
//        Optional<CourseAttempt> courseAttemptOpt = courseAttemptRepository.findById(courseAttemptId);
//        if (courseAttemptOpt.isEmpty()) {
//            response.put("message", "Invalid Course Attempt ID");
//            response.put("error", true);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        }
//        // Generate Zoom access token
//        String accessToken = zoomService.generateAccessToken();
//        Map<String, Object> userDetails = zoomService.getUserDetails(accessToken);
//        String userId = (String) userDetails.get("id");
//        // Prepare data for Zoom API
//        Map<String, Object> meetingData = new HashMap<>();
//        meetingData.put("topic", meetingDTO.getTopic());
//        meetingData.put("type", meetingDTO.getType());
//        meetingData.put("start_time", meetingDTO.getStartTime()); // No conversion needed
//        meetingData.put("duration", meetingDTO.getDuration());
//        meetingData.put("timezone", meetingDTO.getTimezone());
//        meetingData.put("agenda", meetingDTO.getAgenda());
//        Map<String, Object> settingsMap = new HashMap<>();
//        settingsMap.put("host_video", meetingDTO.getSettings().getHostVideo());
//        settingsMap.put("participant_video", meetingDTO.getSettings().getParticipantVideo());
//        settingsMap.put("join_before_host", meetingDTO.getSettings().getJoinBeforeHost());
//        settingsMap.put("mute_upon_entry", meetingDTO.getSettings().getMuteUponEntry());
//        settingsMap.put("waiting_room", meetingDTO.getSettings().getWaitingRoom());
//        meetingData.put("settings", settingsMap);
//        // Create meeting via Zoom API
//        Map<String, Object> meetingResponse = zoomService.createMeeting(accessToken, userId, meetingData);
//        // Extract meeting details
//        String meetingId = String.valueOf(meetingResponse.get("id"));
//        String meetingUrl = (String) meetingResponse.get("join_url");
//        String startUrl = (String) meetingResponse.get("start_url");
//        // Save meeting to database
//        Meeting meeting = new Meeting();
//        meeting.setMeetingId(meetingId);
//        meeting.setMeetingUrl(meetingUrl);
//        meeting.setTopic(meetingDTO.getTopic());
//        meeting.setStartTime(meetingDTO.getStartTime()); // No conversion needed
//        meeting.setDuration(meetingDTO.getDuration());
//        meeting.setAgenda(meetingDTO.getAgenda());
//        meeting.setCourseAttempt(courseAttemptOpt.get());
//        meeting.setStartUrl(startUrl);
//        meetingRepository.save(meeting);
//        // Prepare response
//        response.put("message", "Meeting created successfully.");
//        response.put("meetingUrl", meetingUrl);
//        response.put("startUrl", startUrl);
//        response.put("meetingId", meetingId);
//        return ResponseEntity.ok(response);
//    }
//
//    @Operation(summary = "List Meetings")
//    @GetMapping("/list-meetings")
//    public ResponseEntity<List<Map<String, Object>>> listMeetings(
//            @RequestParam(name = "status", required = false) String status) {
//
//        List<Meeting> meetings = meetingRepository.findAll();
//
//        List<Map<String, Object>> upcomingMeetings = new ArrayList<>();
//        List<Map<String, Object>> completedMeetings = new ArrayList<>();
//        List<Map<String, Object>> canceledMeetings = new ArrayList<>();
//
//        LocalDateTime now = LocalDateTime.now();
//
//        for (Meeting meeting : meetings) {
//            Map<String, Object> meetingMap = new HashMap<>();
//            meetingMap.put("meetingId", meeting.getMeetingId());
//            meetingMap.put("meetingUrl", meeting.getMeetingUrl());
//            meetingMap.put("topic", meeting.getTopic());
//            meetingMap.put("startTime", meeting.getStartTime());
//            meetingMap.put("startUrl", meeting.getStartUrl());
//            meetingMap.put("duration", meeting.getDuration());
//            meetingMap.put("agenda", meeting.getAgenda());
//
//            if (meeting.isCanceled()) {
//                canceledMeetings.add(meetingMap);
//            } else {
//                LocalDateTime startTime = LocalDateTime.parse(meeting.getStartTime().substring(0, 19));
//
//                if (startTime.isAfter(now)) {
//                    upcomingMeetings.add(meetingMap);
//                } else {
//                    completedMeetings.add(meetingMap);
//                }
//            }
//        }
//
//        if ("upcoming".equalsIgnoreCase(status)) {
//            return ResponseEntity.ok(upcomingMeetings);
//        } else if ("completed".equalsIgnoreCase(status)) {
//            return ResponseEntity.ok(completedMeetings);
//        } else if ("canceled".equalsIgnoreCase(status)) {
//            return ResponseEntity.ok(canceledMeetings);
//        }
//
//        List<Map<String, Object>> allMeetings = new ArrayList<>();
//        allMeetings.addAll(upcomingMeetings);
//        allMeetings.addAll(completedMeetings);
//        allMeetings.addAll(canceledMeetings);
//
//        return ResponseEntity.ok(allMeetings);
//    }
//
//    @Operation(summary = "Cancel a Zoom Meeting")
//    @PutMapping("/cancel")
//    public ResponseEntity<Map<String, Object>> cancelMeeting(@RequestParam String meetingId) {
//        String accessToken = zoomService.generateAccessToken();
//
//        // Define the Zoom API endpoint
//        String url = "https://api.zoom.us/v2/meetings/" + meetingId;
//
//        // Set up headers
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//
//        // Send DELETE request
//        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);
//
//        Map<String, Object> responseBody = new HashMap<>();
//        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
//            // Update database
//            Optional<Meeting> meetingOpt = meetingRepository.findByMeetingId(meetingId);
//            if (meetingOpt.isPresent()) {
//                Meeting meeting = meetingOpt.get();
//                meeting.setCanceled(true);
//                meetingRepository.save(meeting);
//            }
//
//            responseBody.put("message", "Meeting canceled successfully");
//            return ResponseEntity.ok(responseBody);
//        } else {
//            responseBody.put("error", "Failed to cancel meeting");
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
//        }
//    }
//
//    @Operation(summary = "Get Meeting Details")
//    @GetMapping("/meeting-details/{meetingId}")
//    public Map<String, Object> getMeetingDetails(@PathVariable String meetingId) {
//        String accessToken = zoomService.generateAccessToken();
//        return zoomService.getMeetingDetails(accessToken, meetingId);
//    }
//
//    @Operation(summary = "Update Meeting")
//    @PatchMapping("/update-meeting/{meetingId}")
//    public void updateMeeting(@PathVariable String meetingId, @RequestBody Map<String, Object> updatedData) {
//        String accessToken = zoomService.generateAccessToken();
//        zoomService.updateMeeting(accessToken, meetingId, updatedData);
//    }
//
//    @Operation(summary = "Delete Meeting")
//    @DeleteMapping("/delete-meeting/{meetingId}")
//    public void deleteMeeting(@PathVariable String meetingId) {
//        String accessToken = zoomService.generateAccessToken();
//        zoomService.deleteMeeting(accessToken, meetingId);
//    }
//
//    @Operation(summary = "Join a Zoom Meeting")
//    @GetMapping("/join-meeting")
//    public ResponseEntity<Map<String, Object>> joinMeeting(
//            @RequestParam String meetingId,
//            @RequestParam UUID studentId) {
//
//        Map<String, Object> response = new HashMap<>();
//        Optional<Meeting> meetingOpt = meetingRepository.findByMeetingId(meetingId);
//        if (meetingOpt.isEmpty()) {
//            response.put("message", "Meeting not found.");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//        Meeting meeting = meetingOpt.get();
//        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByStudentIdAndCourseAttemptId(studentId, meeting.getCourseAttempt().getId());
//        if (enrollmentOpt.isEmpty()) {
//            response.put("message", "Access denied. You are not enrolled in this course.");
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
//        }
//        response.put("message", "Join the meeting using the provided URL.");
//        response.put("meetingUrl", meeting.getMeetingUrl());
//        response.put("meetingId", meeting.getMeetingId());
//        response.put("topic", meeting.getTopic());
//        response.put("startTime", meeting.getStartTime());
//        response.put("duration", meeting.getDuration());
//        response.put("agenda", meeting.getAgenda());
//        return ResponseEntity.ok(response);
//    }
//
//        @Operation(summary = "Get Joined Meetings")
//        @GetMapping("/joined-meetings")
//        public ResponseEntity<List<Map<String, Object>>> getJoinedMeetings(@RequestParam UUID studentId) {
//            List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
//
//            if (enrollments.isEmpty()) {
//                return ResponseEntity.ok(Collections.emptyList());
//            }
//
//            List<Map<String, Object>> meetingsList = new ArrayList<>();
//            for (Enrollment enrollment : enrollments) {
//                List<Meeting> meetings = meetingRepository.findByCourseAttemptId(enrollment.getCourseAttempt().getId());
//
//                for (Meeting meeting : meetings) {
//                    Map<String, Object> meetingData = new HashMap<>();
//                    meetingData.put("meetingId", meeting.getMeetingId());
//                    meetingData.put("courseName", meeting.getCourseAttempt().getCourseType().getCourse().getTitle());
//                    meetingData.put("topic", meeting.getTopic());
//                    meetingData.put("startTime", meeting.getStartTime());
//                    meetingData.put("duration", meeting.getDuration());
//                    meetingData.put("meetingUrl", meeting.getMeetingUrl());
//
//                    meetingsList.add(meetingData);
//                }
//            }
//
//            return ResponseEntity.ok(meetingsList);
//        }
//
//    @Operation(summary = "Get Meetings for Student")
//    @GetMapping("/student/{studentId}")
//    public ResponseEntity<ApiResponseWithMeta> getMeetingsForStudent(
//            @PathVariable("studentId") UUID studentId,
//            @RequestParam(name = "status", required = false) String status,
//            @RequestParam(name = "size", required = false) Integer size) {
//        return ResponseEntity.ok(zoomService.getMeetingsForStudent(studentId, status, size));
//    }
//
//    @Operation(summary = "Get Meetings for Student by Course")
//    @GetMapping("/meetings/by-course")
//    public ResponseEntity<ApiResponseWithMeta> getMeetingsForStudentByCourse(
//            @RequestParam UUID studentId,
//            @RequestParam UUID courseAttemptId) {
//        return ResponseEntity.ok(zoomService.getMeetingsForStudentByCourse(studentId, courseAttemptId));
//    }
//
//    @Operation(summary = "Get Meetings by Course Attempt")
//    @GetMapping("/by-course-attempt")
//    public ResponseEntity<ApiResponseWithMeta> getMeetingsByCourseAttempt(
//            @RequestParam("courseAttemptId") UUID courseAttemptId) {
//        return zoomService.getMeetingsByCourseAttempt(courseAttemptId);
//    }
//
//}
//
//
