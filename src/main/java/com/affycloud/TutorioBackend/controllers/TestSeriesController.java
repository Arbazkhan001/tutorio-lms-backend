//package com.affycloud.TutorioBackend.controllers;
//
//import com.affycloud.TutorioBackend.dtos.exam.ExamDTO;
//import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
//import com.affycloud.TutorioBackend.dtos.testSeries.TestSeriesAddDto;
//import com.affycloud.TutorioBackend.dtos.testSeries.TestSeriesResponseDto;
//import com.affycloud.TutorioBackend.dtos.testSeries.TestSeriesResponseWithoutTestsDto;
//import com.affycloud.TutorioBackend.dtos.testSeries.TestSeriesUpdateDto;
//import com.affycloud.TutorioBackend.dtos.testSeries.directory.DirectoryDTO;
//import com.affycloud.TutorioBackend.dtos.testSeries.directory.DirectoryResponseDto;
//import com.affycloud.TutorioBackend.dtos.testSeries.exams.TestSeriesResponseWithExamAndTestDto;
//import com.affycloud.TutorioBackend.dtos.testSeries.test.TestAddInTestSeriesDto;
//import com.affycloud.TutorioBackend.handler.ResourceNotFoundException;
//import com.affycloud.TutorioBackend.models.Student;
//import com.affycloud.TutorioBackend.models.User;
//import com.affycloud.TutorioBackend.repositories.AdminRepository;
//import com.affycloud.TutorioBackend.repositories.TeacherRepository;
//import com.affycloud.TutorioBackend.services.TestSeriesService;
//import com.affycloud.TutorioBackend.utils.AuthUtilAdmin;
//import io.swagger.v3.oas.annotations.Operation;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.io.IOException;
//import java.util.*;
//
//
//@RestController
//@RequestMapping("/api/testSeries")
//@RequiredArgsConstructor
//public class TestSeriesController {
//
//    @Autowired
//    private TestSeriesService testSeriesService;
//
//    @Autowired
//    private TeacherRepository teacherRepository;
//
//    @Autowired
//    private AdminRepository adminRepository;
//
//    @Autowired
//    private AuthUtilAdmin authUtil;
//
//
//    @Operation(summary = "creating test series")
//    @PostMapping(value = "create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<Map<String, Object>> createTestSeries(@ModelAttribute TestSeriesAddDto request,
//                                                                HttpServletRequest httpRequest) throws IOException {
//        User user = authUtil.getAuthenticatedUser(httpRequest);
//        Map<String, Object> response = new HashMap<>();
//        try {
//            response = testSeriesService.createTestSeries(request,user);
//            // If the response contains an error key, handle it accordingly
//            if (response.containsKey("error") && !(boolean) response.get("error")) {
//                return ResponseEntity.ok().body(response);
//            } else {
//                // If error is true, return a BAD_REQUEST with the error message
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//            }
//        } catch (IllegalArgumentException e) {
//            // Specific handling for invalid arguments (e.g., user not found or invalid price)
//            response.put("error", true);
//            response.put("message", e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        } catch (Exception e) {
//            // General exception handling for unexpected errors
//            response.put("error", true);
//            response.put("message", "Unexpected error: " + e.getMessage());
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
//        }
//    }
//
//
//    @Operation(summary = "Adding test into test series")
//    @PostMapping("/test/{testSeriesId}")
//    public ResponseEntity<Map<String, Object>> addTestToTestSeries(
//            @PathVariable("testSeriesId") UUID testSeriesId,
//            @RequestBody TestAddInTestSeriesDto request) {
//        return ResponseEntity.ok(testSeriesService.addTestToTestSeries(testSeriesId, request));
//    }
//
//    @Operation(summary = "Adding test into test series")
//    @PostMapping("/exam/{testSeriesId}")
//    public ResponseEntity<Map<String, Object>> addExamToTestSeries(
//            @PathVariable("testSeriesId") UUID testSeriesId,
//            @RequestBody ExamDTO request) {
//        return ResponseEntity.ok(testSeriesService.addExamToTestSeries(testSeriesId, request));
//    }
//
//
//    @Operation(summary = "Add a new directory to a test series")
//    @PostMapping("/directory/{testSeriesId}")
//    public ResponseEntity<Map<String, Object>> addDirectoryInTestSeries(
//            @PathVariable("testSeriesId") UUID testSeriesId,
//            @RequestBody DirectoryDTO request) {
//        return ResponseEntity.ok(testSeriesService.addDirectoryInTestSeries(testSeriesId, request));
//    }
//
//    @Operation(summary = "Add test to a directory")
//    @PostMapping("/directory/test/{directoryId}")
//    public ResponseEntity<Map<String, Object>> addTestInDirectory(
//            @PathVariable("directoryId") UUID directoryId,
//            @RequestBody TestAddInTestSeriesDto request) {
//        return ResponseEntity.ok(testSeriesService.addTestInDirectory(directoryId, request));
//    }
//
//    @Operation(summary = "Add exam to a directory")
//    @PostMapping("/directory/exam/{directoryId}")
//    public ResponseEntity<Map<String, Object>> addExamInDirectory(
//            @PathVariable("directoryId") UUID directoryId,
//            @RequestBody ExamDTO request) {
//        return ResponseEntity.ok(testSeriesService.addExamInDirectory(directoryId, request));
//    }
//
//    @Operation(summary = "Get test series by id")
//    @GetMapping("/{id}")
//    public ResponseEntity<Map<String, Object>> getTestSeriesById(@PathVariable("id") UUID id,
//                                                                 @RequestHeader(value = "subdomain", required = true) String subdomain,
//                                                                 HttpServletRequest httpRequest) {
//
//        UUID tenantId = authUtil.validateAndGetUser(subdomain);
//        Student student = authUtil.getOptionalAuthenticatedStudent(httpRequest);
//        try {
//            Map<String, Object> response = new HashMap<>();
//            TestSeriesResponseDto testSeries;
//            if (student != null) {
//                testSeries = testSeriesService.getTestSeriesById(id, tenantId,student.getId());
//            } else {
//                testSeries = testSeriesService.getTestSeriesById(id,tenantId);
//            }
//            response.put("meta", testSeries);
//            response.put("error", false);
//            response.put("message", "Test series retrieved successfully");
//            return ResponseEntity.ok(response);
//        } catch (RuntimeException e) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("error", true);
//            errorResponse.put("message", e.getMessage());
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
//        }
//    }
//
//    @Operation(summary = "Get test series by id with their test and exams, also check if student attended the test")
//    @GetMapping("/content/{id}")
//    public ResponseEntity<Map<String, Object>> getTestSeriesById2(
//            @PathVariable("id") UUID id,
//            @RequestParam(value = "studentId", required = false) UUID studentId) {
//        try {
//            TestSeriesResponseWithExamAndTestDto testSeries = testSeriesService.getTestSeriesByIdWithExamsAndTest(id, studentId);
//            Map<String, Object> response = new HashMap<>();
//            response.put("error", false);
//            response.put("message", "Test series retrieved successfully");
//            response.put("meta", testSeries);
//            return ResponseEntity.ok(response);
//        } catch (RuntimeException e) {
//            Map<String, Object> errorResponse = new HashMap<>();
//            errorResponse.put("error", true);
//            errorResponse.put("message", e.getMessage());
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
//        }
//    }
//
//    @Operation(summary = "Get test series by userId")
//    @GetMapping("/createdBy/{userId}")
//    public ResponseEntity<List<TestSeriesResponseDto>> getTestSeriesByUserId(@PathVariable UUID userId) {
//        List<TestSeriesResponseDto> testSeriesList = testSeriesService.getTestSeriesByUserId(userId);
//        return ResponseEntity.ok(testSeriesList);
//    }
//
//    @Operation(summary = "Delete test series by id")
//    @DeleteMapping("/{id}")
//    public ResponseEntity<ApiResponse> deleteTestSeries(@PathVariable("id") UUID id) {
//        try {
//            testSeriesService.deleteTestSeries(id);
//            ApiResponse response = new ApiResponse("Test-series deleted successfully.", false);
//            return ResponseEntity.ok(response);
//        } catch (ResponseStatusException ex) {
//            ApiResponse response = new ApiResponse(ex.getReason(), true);
//            return ResponseEntity.status(ex.getStatusCode()).body(response);
//        }
//    }
//
//    @Operation(
//            summary = "Update an existing Course Bundle",
//            description = "Updates price, discountType, discount, finalPrice, title, description, and thumbnail of a course bundle. Only provided fields will be updated."
//    )
//    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> updateCourseBundle(
//            @PathVariable UUID id,
//            @ModelAttribute("series") TestSeriesUpdateDto updateDto,
//            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
//        try {
//            Map<String, Object> response = testSeriesService.updateTestSeries(id, updateDto, thumbnail);
//            if (response.containsKey("error") && (boolean) response.get("error")) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//            }
//            return ResponseEntity.ok(response);
//        }catch (ResourceNotFoundException ex) {
//            // Handling exception for * not found
//            Map<String, Object> response = new LinkedHashMap<>();
//            response.put("error", true);
//            response.put("message", ex.getMessage()); // Return the exception message (e.g., "* not found")
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    Map.of("message", "Unexpected error: " + e.getMessage(), "error", true)
//            );
//        }
//    }
//
//    @Operation(summary = "List of test series")
//    @GetMapping("/list")
//    public ResponseEntity<?> getAllTestSeries(
//                                              @RequestHeader(value = "subdomain", required = true) String subdomain,
//                                              @RequestParam(defaultValue = "1") int page,
//                                              @RequestParam(defaultValue = "10") int size,
//                                              @RequestParam(required = false) String paymentType,
//                                              HttpServletRequest httpRequest) { // new filter parameter
//        UUID tenantId = authUtil.validateAndGetUser(subdomain);
//
//        Student student = authUtil.getOptionalAuthenticatedStudent(httpRequest);
//        Map<String, Object> response = new HashMap<>();
//        try {
//            Pageable pageable = PageRequest.of(page-1, size,Sort.by(Sort.Direction.DESC, "createdAt"));
//            if (student != null) {
//                Page<Map<String, Object>> testSeriesPage = testSeriesService.getAllTestSeriesByStudent(pageable, student.getId(), tenantId, paymentType);
//                response.put("meta", testSeriesPage.getContent());
//                response.put("currentPage", testSeriesPage.getNumber());
//                response.put("totalItems", testSeriesPage.getTotalElements());
//                response.put("totalPages", testSeriesPage.getTotalPages());
//            } else {
//                Page<TestSeriesResponseDto> testSeriesPage = testSeriesService.getAllTestSeries(pageable,tenantId,paymentType);
//                response.put("meta", testSeriesPage.getContent());
//                response.put("currentPage", testSeriesPage.getNumber());
//                response.put("totalItems", testSeriesPage.getTotalElements());
//                response.put("totalPages", testSeriesPage.getTotalPages());
//            }
//            response.put("message", "Tests fetched successfully");
//            response.put("error", false);
//            return ResponseEntity.ok(response);
//        } catch (ResponseStatusException ex) {
//            response.put("message", ex.getReason());
//            response.put("error", true);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//    }
//
//    @Operation(summary = "List of test series with test counts")
//    @GetMapping("/list/count")
//    public ResponseEntity<?> getAllTestSeriesWithTestsCounts(@RequestParam(defaultValue = "1") int page,
//                                                             @RequestParam(defaultValue = "10") int size,
//                                                             HttpServletRequest httpRequest,
//                                                             @RequestParam(required = false) UUID tenantId) {
//
//        Map<String, Object> response = new HashMap<>();
//        UUID id;
//        if(tenantId != null){
//            Optional<User> user = teacherRepository.findById(tenantId);
//            if (user.isPresent()) {
//                id = tenantId;
//            } else {
//                response.put("message", "Parent not found");
//                response.put("error", true);
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//            }
//
//        }
//        else {
//            User user = authUtil.getAuthenticatedUser(httpRequest);
//            id = user.getId();
//        }
//        try {
//            Pageable pageable = PageRequest.of(page-1, size);
//            Page<TestSeriesResponseWithoutTestsDto> testSeriesPage = testSeriesService.getAllTestSeriesWithTestCount(pageable,id);
//
//            response.put("meta", testSeriesPage.getContent());
//            response.put("currentPage", testSeriesPage.getNumber());
//            response.put("totalItems", testSeriesPage.getTotalElements());
//            response.put("totalPages", testSeriesPage.getTotalPages());
//            response.put("message", "Tests fetched successfully");
//            response.put("error", false);
//
//            return ResponseEntity.ok(response);
//        } catch (ResponseStatusException ex) {
//            response.put("message", ex.getReason());
//            response.put("error", true);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//    }
//
//    @Operation(summary = "Get directory details by directoryId")
//    @GetMapping("/directory/{directoryId}")
//    public ResponseEntity<DirectoryResponseDto> getDirectoryDetailsByDirectoryId(@PathVariable("directoryId") UUID directoryId) {
//        return ResponseEntity.ok(testSeriesService.getDirectoryDetailsByDirectoryId(directoryId));
//    }
//
//}