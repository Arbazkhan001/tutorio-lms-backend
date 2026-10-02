//package com.affycloud.TutorioBackend.controllers;
//
//import com.affycloud.TutorioBackend.dtos.doubt.*;
//import com.affycloud.TutorioBackend.dtos.reponse.PaginatedResponse;
//import com.affycloud.TutorioBackend.services.DoubtService;
//import io.swagger.v3.oas.annotations.Operation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.*;
//
//
//@RestController
//@RequestMapping("/api/doubts")
//public class DoubtController {
//
//    @Autowired
//    private DoubtService doubtService;
//
//    @Operation(summary = "Create doubt conversation for a student")
//    @PostMapping("/create")
//    public ResponseEntity<Map<String, Object>> createConversation(@RequestBody CreateConversationRequest request) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            // For creation, the sender is usually the student (senderId equals studentId)
//            DoubtConversationDto conversation = doubtService.createConversation(
//                    request.getCourseTypeId(),
//                    request.getSenderId(),
//                    request.getMessageText(),
//                    request.getSenderId(),  // senderId is studentId in this case
//                    request.getSenderRole()
//            );
//            response.put("message", "Message sent successfully");
//            response.put("error", false);
//            return ResponseEntity.ok(response);
//        } catch (Exception ex) {
//            response.put("message", ex.getMessage());
//            response.put("error", true);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        }
//    }
//
//    @Operation(summary = "Add a new message to a conversation")
//    @PostMapping("/messages/{conversationId}")
//    public ResponseEntity<Map<String, Object>> addMessage(@PathVariable UUID conversationId,
//                                                          @RequestBody AddMessageRequest request) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            DoubtMessageDto message = doubtService.addMessage(
//                    conversationId,
//                    request.getSenderId(),
//                    request.getSenderRole(),
//                    request.getMessageText()
//            );
//            response.put("message", "Message added successfully");
//            response.put("error", false);
//            return ResponseEntity.ok(response);
//        } catch (Exception ex) {
//            response.put("message", ex.getMessage());
//            response.put("error", true);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        }
//    }
//
//    @Operation(summary = "Get all latest doubts")
//    @GetMapping("/latest")
//    public ResponseEntity<Map<String, Object>> getAllLatestDoubts() {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            List<LatestDoubtMessageDto> latestDoubts = doubtService.getAllLatestDoubts();
//            response.put("meta", latestDoubts);
//            response.put("message", "Latest doubts fetched successfully");
//            response.put("error", false);
//            return ResponseEntity.ok(response);
//        } catch (Exception ex) {
//            response.put("message", ex.getMessage());
//            response.put("error", true);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        }
//    }
//
//
//    @Operation(summary = "Get conversation by ID")
//    @GetMapping("/{conversationId}")
//    public ResponseEntity<Map<String, Object>> getConversation(@PathVariable UUID conversationId) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            DoubtConversationDto conversation = doubtService.getConversation(conversationId);
//            response.put("meta", conversation);
//            response.put("message", "Conversation fetched successfully");
//            response.put("error", false);
//            return ResponseEntity.ok(response);
//        } catch (Exception ex) {
//            response.put("message", ex.getMessage());
//            response.put("error", true);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//    }
//
//    @Operation(summary = "Get all course types for doubts")
//    @GetMapping("/course-types")
//    public ResponseEntity<Map<String, Object>> getAllCourseTypes() {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            List<DoubtCourseDetailDto> courseTypes = doubtService.getAllCourseTypes();
//            response.put("meta", courseTypes);
//            response.put("message", "Course types fetched successfully");
//            response.put("error", false);
//            return ResponseEntity.ok(response);
//        } catch (Exception ex) {
//            response.put("message", ex.getMessage());
//            response.put("error", true);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        }
//    }
//
//    @Operation(summary = "Get all student list by courseTypeId")
//    @GetMapping("/course/students/{courseTypeId}")
//    public ResponseEntity<Map<String, Object>> getAllStudentsByCourseTypeId(@PathVariable UUID courseTypeId) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            List<DoubtConversationStudentsListDto> conversations = doubtService.getConversationsByCourseTypeId(courseTypeId);
//            response.put("meta", conversations);
//            response.put("message", "Students fetched successfully");
//            response.put("error", false);
//            return ResponseEntity.ok(response);
//        } catch (Exception ex) {
//            response.put("message", ex.getMessage());
//            response.put("error", true);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//
//    }
//
//    @Operation(summary = "Get conversation messages by studentId and courseTypeId with pagination")
//    @GetMapping("/student/{studentId}/course/{courseTypeId}")
//    public ResponseEntity<Map<String, Object>> getConversationMessagesByStudentAndCourse(
//            @PathVariable UUID studentId,
//            @PathVariable UUID courseTypeId,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        try {
//            PaginatedResponse<DoubtMessageDto> messagesPage = doubtService
//                    .getPaginatedMessagesByStudentAndCourse(studentId, courseTypeId, page - 1, size);
//
//            // Flatten the PaginatedResponse into a single map with all fields at root level
//            Map<String, Object> response = new LinkedHashMap<>();
//            response.put("totalPages", messagesPage.getTotalPages());
//            response.put("totalItems", messagesPage.getTotalItems());
//            response.put("size", messagesPage.getSize());
//            response.put("currentPage", messagesPage.getCurrentPage() + 1);
//            response.put("meta", messagesPage.getMeta());
//            response.put("message", "Conversation messages fetched successfully");
//            response.put("error", false);
//            return ResponseEntity.ok(response);
//        } catch (Exception ex) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("message", ex.getMessage());
//            errorResponse.put("error", true);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
//        }
//    }
//
//    @Operation(summary = "Get all students for doubts")
//    @GetMapping("/students")
//    public ResponseEntity<Map<String, Object>> getAllStudents() {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            List<SenderStudentDetailDto> courseTypes = doubtService.getAllStudents();
//            response.put("meta", courseTypes);
//            response.put("message", "students fetched successfully");
//            response.put("error", false);
//            return ResponseEntity.ok(response);
//        } catch (Exception ex) {
//            response.put("message", ex.getMessage());
//            response.put("error", true);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        }
//    }
//
//}
//
