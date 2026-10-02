package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.course.CourseDetailDto;
import com.affycloud.TutorioBackend.dtos.courseBundle.*;
import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.enums.ItemType;
import com.affycloud.TutorioBackend.handler.InvalidOperationException;
import com.affycloud.TutorioBackend.handler.ResourceNotFoundException;
import com.affycloud.TutorioBackend.models.*;
import com.affycloud.TutorioBackend.repositories.*;
import com.affycloud.TutorioBackend.services.CourseBundleService;
import com.affycloud.TutorioBackend.services.CourseService;
import com.affycloud.TutorioBackend.utils.AuthUtilAdmin;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;


@RestController
@RequestMapping("/api/course-bundles")
public class CourseBundleController {

    @Autowired
    private CourseBundleService bundleService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private TeacherRepository teacherRepository;


    @Autowired
    private AuthUtilAdmin authUtil;


    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Creating a new Course Bundle with a thumbnail image",
            description = "Create a new Course Bundle and upload an optional thumbnail image"
    )
    public ResponseEntity<?> createCourseBundle(
            @ModelAttribute CourseBundleAddDto request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            HttpServletRequest httpRequest) throws IOException {
        User user = authUtil.getAuthenticatedUser(httpRequest);
            Map<String, Object> response = bundleService.createCourseBundle(request,thumbnail,user);
            /* Storing value from the response. */
            Object value = response.get("message");
            // If there is no error, return 200 OK with the response
            if (response.containsKey("error") && !(boolean) response.get("error")) {
                return ResponseEntity.ok().body(response);
            }
            // If there is an error, return 400 BAD_REQUEST or 409 CONFLICT depending on the case
            else {
                String errorResponse = (String) value;
                if (errorResponse.contains("already exists")) {
                    return ResponseEntity.status(HttpStatus.IM_USED).body(response);
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Update an existing Course Bundle",
            description = "Updates price, discountType, discount, finalPrice, title, description, and thumbnail of a course bundle. Only provided fields will be updated."
    )
    public ResponseEntity<?> updateCourseBundle(
            @PathVariable UUID id,
            @ModelAttribute("bundle") CourseBundleUpdateDto updateDto,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) {
        try {
            Map<String, Object> response = bundleService.updateCourseBundle(id, updateDto, thumbnail);
            if (response.containsKey("error") && (boolean) response.get("error")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            return ResponseEntity.ok(response);
        }catch (ResourceNotFoundException ex) {
            // Handling exception for * not found
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("error", true);
            response.put("message", ex.getMessage()); // Return the exception message (e.g., "* not found")
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Unexpected error: " + e.getMessage(), "error", true)
            );
        }
    }

    @PostMapping("/add-course-attempt/{bundleId}")
    @Operation(
            summary = "Add a Course Attempt to a Bundle",
            description = "Adds a new course attempt to the specified bundle."
    )
    public ResponseEntity<?> addCourseAttemptToBundle(
            @PathVariable UUID bundleId,
            @RequestParam UUID courseAttemptId) {
        try {
            Map<String, Object> response = bundleService.addCourseAttemptToBundle(bundleId, courseAttemptId);
            return ResponseEntity.ok(response);
        }catch (ResourceNotFoundException ex) {
            // Handling exception for * not found
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("error", true);
            response.put("message", ex.getMessage()); // Return the exception message (e.g., "* not found")

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (InvalidOperationException ex) {
            // Handling exception for * not found
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("error", true);
            response.put("message", ex.getMessage()); // Return the exception message (e.g., "* not found")
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unexpected error: " + e.getMessage(), "error", true));
        }
    }

    @DeleteMapping("/remove-course-attempt/{bundleId}")
    @Operation(
            summary = "Remove a Course Attempt from a Bundle",
            description = "Removes an existing course attempt from the specified bundle."
    )
    public ResponseEntity<?> removeCourseAttemptFromBundle(
            @PathVariable UUID bundleId,
            @RequestParam UUID courseAttemptId) {
        try {
            Map<String, Object> response = bundleService.removeCourseAttemptFromBundle(bundleId, courseAttemptId);
            return ResponseEntity.ok(response);
        }catch (ResourceNotFoundException ex) {
            // Handling exception for * not found
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("error", true);
            response.put("message", ex.getMessage()); // Return the exception message (e.g., "* not found")
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Unexpected error: " + e.getMessage(), "error", true));
        }
    }

    @Operation(summary = "Get a course bundle by its ID and optional student ID for enrolled courses")
    @GetMapping("/{bundleId}")
    public ResponseEntity<?> getBundleById(@PathVariable UUID bundleId,
                                           @RequestHeader(value = "subdomain", required = true) String subdomain,
                                           HttpServletRequest httpRequest) {

        UUID tenantId = authUtil.validateAndGetUser(subdomain);
        Student student = authUtil.getOptionalAuthenticatedStudent(httpRequest);


        Map<String, Object> response = new HashMap<>();
        try {
            CourseBundleDetailDto bundleDto;
            if (student != null) {
                bundleDto = bundleService.getBundleById(bundleId, tenantId, student.getId());
            } else {
                bundleDto = bundleService.getBundleById(bundleId,tenantId);
            }
            response.put("meta", bundleDto);
            response.put("message", "Bundle fetched successfully");
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", "true");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }


 /*   @Operation(summary = "Get all course bundles")
    @GetMapping("/all")
    public ResponseEntity<ApiResponseWithMeta> getAllBundles(HttpServletRequest httpRequest) {
        User user = authUtil.getAuthenticatedUser(httpRequest);
        List<CourseBundle> bundles = bundleService.getAllBundles(user.getId());
        List<CourseBundleSummaryDetailDto> dtos = bundles.stream()
                .map(CourseBundleSummaryDetailDto::fromEntity)
                .collect(Collectors.toList());
        Map<String, Object> meta = new HashMap<>();
        meta.put("data", dtos);
        return ResponseEntity.ok(new ApiResponseWithMeta("Course bundles fetched successfully.", false, meta));
    }*/

    @Operation(summary = "Get all course bundles")
    @GetMapping("/all")
    public ResponseEntity<?> getAllBundles(HttpServletRequest httpRequest,
                                           @RequestParam(required = false) UUID tenantId) {

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
            List<CourseBundleSummaryDetailDto> bundles = bundleService.getAllBundles(id);
            response.put("meta", bundles);  // "meta" contains the bundles details
            response.put("message", "Course bundles fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }


    @Operation(summary = "Get combined courses and bundles with optional filters and pagination and optional student ID for enrolled courses")
    @GetMapping("/all/courses-bundles")
    public ResponseEntity<ApiResponseWithMeta> getCombinedCoursesAndBundles(
            @RequestHeader(value = "subdomain", required = true) String subdomain,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "collection", required = false) String collection,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "paymentType", required = false) PaymentType paymentType,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        try {
            UUID tenantId = authUtil.validateAndGetUser(subdomain);
            Student student = authUtil.getOptionalAuthenticatedStudent(httpRequest);

            // 👇 Default empty sets in case student is null
            Set<UUID> courseBundleIdsInCart = new HashSet<>();
            Set<UUID> courseIdsInCart = new HashSet<>();
            Set<UUID> enrolledCourseIds = new HashSet<>();

            if (student != null) {
                courseBundleIdsInCart = cartItemRepository.findCourseBundleIdsByStudentId(student.getId());
                courseIdsInCart = cartItemRepository.findTestSeriesIdsByStudentId(student.getId());
                enrolledCourseIds = enrollmentRepository.findCourseIdsByStudentId(student.getId());
            }
            List<CourseDetailDto> coursesList = courseService
                    .getCoursesByFiltersWithTenant(category, collection, subject, paymentType,  tenantId);

            List<CourseBundle> bundlesFiltered = bundleService.getBundlesByFiltersByTenant(category, collection, subject,tenantId);
            List<CourseBundleDetailDto> bundleDtos = bundlesFiltered.stream()
                    .map(CourseBundleDetailDto::fromEntity)
                    .toList();

            List<CombinedItemDto> combinedList = new ArrayList<>();
            for (CourseDetailDto course : coursesList) {
                CombinedItemDto item = new CombinedItemDto();
                item.setId(course.getId());
                item.setTitle(course.getTitle());
                item.setPaymentType(course.getPaymentType());
                item.setCourseTypes(course.getCourseTypes());
                item.setThumbnail(course.getThumbnail());
                item.setCreatedAt(course.getCreatedAt());
                item.setItemType(ItemType.COURSE);
                item.setInCart(courseIdsInCart.contains(course.getId())); // inCart flag set karna
                item.setInMyLearning(enrolledCourseIds.contains(course.getId())); // inCart flag set karna
                combinedList.add(item);
            }

            for (CourseBundleDetailDto bundle : bundleDtos) {
                CombinedItemDto item = new CombinedItemDto();
                item.setId(bundle.getId());
                item.setTitle(bundle.getTitle());
                item.setDescription(bundle.getDescription());
                item.setPrice(bundle.getPrice());
                item.setDiscountType(bundle.getDiscountType());
                item.setDiscount(bundle.getDiscount());
                item.setFinalPrice(bundle.getFinalPrice());
                item.setThumbnail(bundle.getThumbnail());
                item.setCreatedAt(bundle.getCreatedAt());
                item.setItemType(ItemType.COURSEBUNDLE);
                item.setInCart(courseBundleIdsInCart.contains(bundle.getId())); // inCart flag set karna
                combinedList.add(item);
            }
            combinedList.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            int totalItems = combinedList.size();
            int totalPages = (int) Math.ceil((double) totalItems / size);
            int fromIndex = Math.min((page - 1) * size, totalItems);
            int toIndex = Math.min(fromIndex + size, totalItems);
            List<CombinedItemDto> paginatedList = combinedList.subList(fromIndex, toIndex);

            Map<String, Object> meta = new HashMap<>();
            meta.put("data", paginatedList);
            meta.put("totalItems", totalItems);
            meta.put("totalPages", totalPages);
            meta.put("currentPage", page);
            return ResponseEntity.ok(new ApiResponseWithMeta("Courses and Bundles fetched successfully.", false, meta));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseWithMeta(ex.getReason(), true, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseWithMeta("Unexpected error: " + e.getMessage(), true, null));
        }
    }

    @Operation(summary = "Delete Bundle by ID it first check it in carts after that it will delete if it is not in cart")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCourseBundle(@PathVariable UUID id) {
        try {
            bundleService.deleteCourseBundle(id);
            ApiResponse response = new ApiResponse("Bundle deleted successfully.", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            ApiResponse response = new ApiResponse(ex.getReason(), true);
            return ResponseEntity.status(ex.getStatusCode()).body(response);
        }
    }

}