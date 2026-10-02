package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.course.*;
import com.affycloud.TutorioBackend.dtos.course.content.CourseContentDTO;
import com.affycloud.TutorioBackend.dtos.course.courseType.*;
import com.affycloud.TutorioBackend.dtos.course.courseVariant.CategoryVariantDto;
import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.models.*;
import com.affycloud.TutorioBackend.repositories.*;
import com.affycloud.TutorioBackend.services.CourseService;
import com.affycloud.TutorioBackend.utils.AuthUtilAdmin;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService service;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private EbookRepository ebookRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentTestResultRepository studentTestResultRepository;

    @Autowired
    private AuthUtilAdmin authUtil;


    @Operation(summary = "Create a Course")
    @PostMapping("create")
    public ResponseEntity<Map<String, Object>> createCourse(@ModelAttribute CourseAddDto request,
                                                            @RequestParam(value = "image", required = false) MultipartFile image, HttpServletRequest httpRequest) throws IOException {
        User user = authUtil.getAuthenticatedUser(httpRequest);

        Map<String, Object> response = service.createCourse(request, image,user);
        /* Storing value from the response. */
        Object value = response.get("response");
        // If there is no error, return 200 OK with the response
        if (response.containsKey("error") && !(boolean) response.get("error")) {
            return ResponseEntity.ok().body(response);
        }
        // If there is an error, return 400 BAD_REQUEST or 409 CONFLICT depending on the case
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(summary = "Update a Course")
    @PutMapping("update/{courseId}")
    public ResponseEntity<Map<String, Object>> updateCourse(
            @PathVariable UUID courseId,
            @ModelAttribute CourseUpdateDto request,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        Map<String, Object> response = service.updateCourse(courseId, request, image);
        Object value = response.get("response");
        if (response.containsKey("error") && !(boolean) response.get("error")) {
            return ResponseEntity.ok().body(response);
        } else {
            String errorResponse = (String) value;
            if (errorResponse.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
    }


    @Operation(summary = "Add new type in course with one attempt")
    @PostMapping("/add-type/{courseId}")
    public ResponseEntity<?> addNewType(@PathVariable UUID courseId, @RequestBody CourseTypeDto courseTypeAddDto) {
        try {
            Map<String, Object> response = service.addNewType(courseId, courseTypeAddDto);
            // Check if there was an error in the response
            if ((boolean) response.getOrDefault("error", true)) {
                // Return a BAD_REQUEST response with the error message from the service
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            // Return OK status if no errors
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Catch any unexpected exception and return a generic internal server error message
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "An unexpected error occurred: " + e.getMessage());
            errorResponse.put("error", true);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Add new attempt in course-type")
    @PostMapping("/add-attempt/{courseTypeId}")
    public ResponseEntity<?> addNewAttempt(@PathVariable UUID courseTypeId, @RequestBody CourseAttemptDto courseTypeAddDto) {
        try {
            Map<String, Object> response = service.createCourseAttempt(courseTypeId, courseTypeAddDto);

            // Check if there was an error in the response
            if ((boolean) response.getOrDefault("error", true)) {
                // Return a BAD_REQUEST response with the error message from the service
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            // Return OK status if no errors
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Catch any unexpected exception and return a generic internal server error message
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "An unexpected error occurred: " + e.getMessage());
            errorResponse.put("error", true);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }


    @Operation(summary = "Add new batch in course attempt")
    @PostMapping("/add-batch/{courseAttemptId}")
    public ResponseEntity<?> addNewBatchInAttempt(@PathVariable UUID courseAttemptId,@Valid @RequestBody CourseBatchDto courseBatchDto) {
        ApiResponse response = service.createCourseBatch(courseAttemptId, courseBatchDto);
        return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Operation(summary = "Update batch in course attempt")
    @PutMapping("/update-batch/{batchId}")
    public ResponseEntity<?> updateBatchDetails(@PathVariable UUID batchId, @Valid @RequestBody CourseBatchUpdateDto courseBatchDto) {
            ApiResponse response = service.updateCourseBatch(batchId, courseBatchDto);
            return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                    .body(response);
    }

    @Operation(summary = "Update a course type with its attempt")
    @PutMapping("/update-course-type/{courseId}/{courseTypeId}/{courseAttemptId}")
    public ResponseEntity<?> updateCourseTypeWithAttempt(@PathVariable UUID courseId, @PathVariable UUID courseTypeId, @PathVariable UUID courseAttemptId, @RequestBody CourseTypeUpdateDto variantDto) {
        try {
            Map<String, Object> response = service.updateCourseTypeWithAttempt(courseId, courseTypeId, courseAttemptId, variantDto);

            // Check if there was an error in the response
            if ((boolean) response.getOrDefault("error", true)) {
                // Return a BAD_REQUEST response with the error message from the service
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            // Return OK status if no errors
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Catch any unexpected exception and return a generic internal server error message
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "An unexpected error occurred: " + e.getMessage());
            errorResponse.put("error", true);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Get Course by ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getCourseById(@PathVariable UUID id,
                                           @RequestHeader(value = "subdomain", required = false) String subdomain,
                                           HttpServletRequest httpRequest) {

        UUID tenantId = authUtil.validateAndGetUser(subdomain);
        Student student = authUtil.getOptionalAuthenticatedStudent(httpRequest);

        Map<String, Object> response = new HashMap<>();
        try {
            CourseDetailDto course;
            if (student != null) {
                course = service.getCourseById(id,tenantId, student.getId());
            } else {
                course = service.getCourseById(id, tenantId);
            }
            response.put("meta", course);
            response.put("message", "Course fetched successfully");
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", "true");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Get Course by ID for admin")
    @GetMapping("/admin/{id}")
    public ResponseEntity<?> getCourseByIdAdmin(@PathVariable UUID id,
                                           HttpServletRequest httpRequest) {

        User admin = authUtil.getAuthenticatedUser(httpRequest);

        Map<String, Object> response = new HashMap<>();
        try {
            CourseDetailDto course;
            course = service.getCourseByIdForAdmin(id, admin.getId());
            response.put("meta", course);
            response.put("message", "Course fetched successfully");
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", "true");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Get Course Batches by attempt Id.")
    @GetMapping("/batch/{attemptId}")
    public ResponseEntity<?> getCourseBatchByAttemptId(@PathVariable UUID attemptId) {


        Map<String, Object> response = new HashMap<>();
        try {
            List<CourseBatchDetailDto> course;
            course = service.getCourseBatchByAttemptId(attemptId);
            response.put("meta", course);
            response.put("message", "Course fetched successfully");
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", "true");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Get All Courses ")
    @GetMapping("/all")
    public ResponseEntity<?> getAllCourses() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<CourseDetailDto> admins = service.getAllCourses();
            response.put("meta", admins);  // "meta" contains the admins details
            response.put("message", "Courses fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Get All Courses Variants this is for navbar of student app to achieve the hierarchy")
    @GetMapping("/all/variants")
    public ResponseEntity<?> getAllCoursesVariant(@RequestHeader(value = "subdomain", required = true) String subdomain) {
        Map<String, Object> response = new HashMap<>();
        try {
            UUID tenantId = authUtil.validateAndGetUser(subdomain);

            List<CategoryVariantDto> variants = service.getAllCoursesVariant(tenantId);
            response.put("meta", variants);  // "meta" contains the variants details
            response.put("message", "Courses fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Get All Courses with optional filters and pagination")
    @GetMapping("/all/filter")
    public ResponseEntity<?> getAllCourses(@RequestParam(value = "category", required = false) String category,
                                           @RequestParam(value = "collection", required = false) String collection,
                                           @RequestParam(value = "subject", required = false) String subject,
                                           @RequestParam(value = "paymentType", required = false) PaymentType paymentType,
                                           @RequestParam(value = "page", defaultValue = "1") int page,
                                           @RequestParam(value = "size", defaultValue = "10") int size,
                                           @RequestHeader(value = "subdomain", required = true) String subdomain,
                                           HttpServletRequest httpRequest) {

        UUID tenantId = authUtil.validateAndGetUser(subdomain);
        Student student = authUtil.getOptionalAuthenticatedStudent(httpRequest);
        Map<String, Object> response = new HashMap<>();
        try {
            Page<CourseDetailDto> courses;
            if (category != null || collection != null || subject != null || paymentType != null) {
            if (student != null) {
                courses = service.getCoursesByFiltersByStudent(category, collection, subject, paymentType,student.getId(), tenantId, page - 1, size);
            } else{
                courses = service.getCoursesByFilters(category, collection, subject, paymentType, tenantId, page - 1, size);
            }
            } else {
                courses = service.getAllActiveCourses( page - 1, size,tenantId);
            }
            response.put("meta", courses.getContent());
            response.put("totalPages", courses.getTotalPages());
            response.put("totalCourses", courses.getTotalElements());
            response.put("currentPage", courses.getNumber() + 1);
            response.put("message", "Courses fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }



    @Operation(summary = "Get All Courses with optional filters and pagination")
    @GetMapping("/admin/filter")
    public ResponseEntity<?> getAllCoursesForAdmin(@RequestParam(value = "category", required = false) String category,
                                           @RequestParam(value = "collection", required = false) String collection,
                                           @RequestParam(value = "subject", required = false) String subject,
                                           @RequestParam(value = "paymentType", required = false) PaymentType paymentType,
                                           @RequestParam(value = "page", defaultValue = "1") int page,
                                           @RequestParam(value = "size", defaultValue = "10") int size,
                                           @RequestParam(required = false) UUID tenantId,
                                           HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        UUID id;
        if(tenantId != null){
            Optional<User> user = teacherRepository.findById(tenantId);
            if (user.isPresent()) {
                id = tenantId;
            } else {
                response.put("message", "Parent not found");
                response.put("error", true);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        }
        else {
            User user = authUtil.getAuthenticatedUser(httpRequest);
            id = user.getId();
        }
        try {
            // Check if a filter value is provided and if it's an unknown value
            Page<CourseDetailDto> courses;
            if (category != null || collection != null || subject != null) {
                courses = service.getCoursesByFiltersForAdmin(category, collection, subject, paymentType, page - 1, size, id);
            }  else{
                courses = service.getAllActiveCourses(page - 1, size,id);
            }

            response.put("meta", courses.getContent());
            response.put("totalPages", courses.getTotalPages());
            response.put("totalCourses", courses.getTotalElements());
            response.put("currentPage", courses.getNumber() + 1);
            response.put("message", "Courses fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Get All Courses with optional filters excluding CourseType details")
    @GetMapping("/all/filter/without-course-type")
    public ResponseEntity<?> getAllCoursesWithFirstCourseType(@RequestParam(value = "category", required = false) String category,
                                                              @RequestParam(value = "collection", required = false) String collection,
                                                              @RequestParam(value = "subject", required = false) String subject) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<CourseDetailWithoutCourseTypeDto> courses;
            // If any filter parameter is provided, do filtered search, otherwise get all courses
            if (category != null || collection != null || subject != null) {
                courses = service.getCoursesByFiltersWithoutCourseType(category, collection, subject);
            } else {
                courses = service.getAllCoursesWithoutCourseType();
            }
            response.put("meta", courses);
            response.put("message", "Courses fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            // Log the exception for debugging
            System.err.println("Exception occurred: " + ex.getMessage());
            response.put("message", "An error occurred: " + ex.getMessage());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Get All Courses with optional filters and pagination")
    @GetMapping("/all/featured")
    public ResponseEntity<?> getAllCourses(
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestHeader(value = "subdomain", required = true) String subdomain) {

        Map<String, Object> response = new HashMap<>();
        UUID tenantId = authUtil.validateAndGetUser(subdomain);
        try {

            // Check if a filter value is provided and if it's an unknown value
            if (filter != null &&
                    !filter.equalsIgnoreCase("latest") &&
                    !filter.equalsIgnoreCase("popular") &&
                    !filter.equalsIgnoreCase("banner")) {
                response.put("message", "unknown value in filter");
                response.put("error", true);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Page<CourseDetailDto> courses;

            if ("latest".equalsIgnoreCase(filter)) {
                // Latest: last 3 courses sorted by createdAt descending
                Pageable latestPageable = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "createdAt"));
                courses = service.getAllActiveFeaturedCourses(latestPageable,tenantId);
            } else if ("popular".equalsIgnoreCase(filter)) {
                // Popular: courses where isPopular is true
                Pageable pageable = PageRequest.of(page - 1, size);
                courses = service.getAllPopularCourses(pageable,tenantId);
            } else if ("banner".equalsIgnoreCase(filter)) {
                // Banner: courses where onBanner is true
                Pageable pageable = PageRequest.of(page - 1, size);
                courses = service.getAllBannerCourses(pageable,tenantId);
            } else {
                // Default: regular active and PAID courses
                courses = service.getAllActiveCourses(page - 1, size,tenantId);
            }

            response.put("meta", courses.getContent());
            response.put("totalPages", courses.getTotalPages());
            response.put("totalCourses", courses.getTotalElements());
            response.put("currentPage", courses.getNumber() + 1);
            response.put("message", "Courses fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Toggle Course Active Status")
    @PutMapping("/toggle-active/{courseId}")
    public ResponseEntity<?> toggleIsActive(@PathVariable UUID courseId) {
        Map<String, Object> response = service.toggleIsActive(courseId);
        if ((boolean) response.getOrDefault("error", true)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Toggle Course Attempt Active Status")
    @PutMapping("/toggle-attempt-active/{courseAttemptId}")
    public ResponseEntity<?> toggleAttemptIsActive(@PathVariable UUID courseAttemptId) {
        Map<String, Object> response = service.toggleAttemptIsActive(courseAttemptId);
        if ((boolean) response.getOrDefault("error", true)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Toggle Course Batch Active Status")
    @PutMapping("/toggle-batch-active/{courseBatchId}")
    public ResponseEntity<?> toggleBatchIsActive(@PathVariable UUID courseBatchId) {
        Map<String, Object> response = service.toggleBatchIsActive(courseBatchId);
        if ((boolean) response.getOrDefault("error", true)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }


    @PutMapping("/update-status/{id}")
    public ResponseEntity<?> updateProductStatus(@PathVariable UUID id, @RequestBody CourseStatusUpdateDto request) {
        try {
            Map<String, Object> response = service.updateProductStatus(id, request);
            Object value = response.get("message");
            // If there is no error, return 200 OK with the response
            if (response.containsKey("error") && !(boolean) response.get("error")) {
                return ResponseEntity.ok().body(response);
            }
            // If there is an error, return 400 BAD_REQUEST or 409 CONFLICT depending on the case
            else {
                String errorResponse = (String) value;
                if (errorResponse.contains("not found")) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }
            // If there is an error, return 400 BAD_REQUEST
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @Operation(summary = "Delete Course")
    @DeleteMapping("/delete/{courseId}")
    public ResponseEntity<Map<String, Object>> deleteCourse(@PathVariable UUID courseId) {
        Map<String, Object> response = service.deleteCourse(courseId);

        // Check if there was an error in the response
        if ((boolean) response.getOrDefault("error", true)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        // Return OK status if no errors
        return ResponseEntity.ok(response);
    }


    @GetMapping("/content/{courseTypeId}")
    public ResponseEntity<Map<String, Object>> getCombinedCourseContent(
            @PathVariable UUID courseTypeId,
            @RequestParam(required = false) UUID studentId) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<Ebook> ebooks = ebookRepository.findByCourseTypeId(courseTypeId,Sort.by("createdAt").descending());
            List<Video> videos = videoRepository.findByCourseTypeId(courseTypeId,Sort.by("createdAt").descending());
            List<Test> tests = testRepository.findByCourseType(courseTypeId, Sort.by(Sort.Direction.DESC, "createdAt"));

            List<CourseContentDTO> ebookDTOs = ebooks.stream()
                    .map(CourseContentDTO::fromEbook)
                    .toList();
            List<CourseContentDTO> videoDTOs = videos.stream()
                    .map(CourseContentDTO::fromVideo)
                    .toList();

            List<CourseContentDTO> testDTOs = tests.stream()
                    .map(test -> CourseContentDTO.fromTest(test, studentTestResultRepository, studentId))
                    .toList();

            List<CourseContentDTO> combined = new ArrayList<>();
            combined.addAll(ebookDTOs);
            combined.addAll(videoDTOs);
            combined.addAll(testDTOs);

            combined.sort(Comparator.comparing(CourseContentDTO::getCreatedAt,
                    Comparator.nullsLast(Comparator.reverseOrder())).reversed());

            response.put("meta", combined);
            response.put("message", "Course content fetched successfully.");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("meta", Collections.emptyList());
            response.put("message", "Error fetching course content: " + e.getMessage());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Delete Course-Attempt by courseAttemptId ")
    @DeleteMapping("/{courseAttemptId}")
    public ResponseEntity<ApiResponse> deleteCourseAttempt(@PathVariable UUID courseAttemptId) {
        try {
            service.deleteCourseAttempt(courseAttemptId);
            ApiResponse response = new ApiResponse("Course-Attempt deleted successfully.", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            ApiResponse response = new ApiResponse(ex.getReason(), true);
            return ResponseEntity.status(ex.getStatusCode()).body(response);
        }
    }

}
