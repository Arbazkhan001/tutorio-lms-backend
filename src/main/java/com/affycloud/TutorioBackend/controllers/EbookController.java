package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.ebook.EbookAddDto;
import com.affycloud.TutorioBackend.dtos.ebook.EbookDetailDto;
import com.affycloud.TutorioBackend.dtos.ebook.EbookUpdateDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.models.Ebook;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.repositories.EbookRepository;
import com.affycloud.TutorioBackend.repositories.TeacherRepository;
import com.affycloud.TutorioBackend.services.EbookService;
import com.affycloud.TutorioBackend.services.implementation.R2ImageServiceImplementation;
import com.affycloud.TutorioBackend.utils.AuthUtilAdmin;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/ebooks")
public class EbookController {

    @Autowired
    private EbookService ebookService;

    @Autowired
    private R2ImageServiceImplementation r2ImageService;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private AuthUtilAdmin authUtil;

    @Autowired
    private EbookRepository ebookRepository;


    @Operation(summary = "Stream ebook PDF securely — requires student authentication")
    @GetMapping("/{id}/stream")
    public void streamEbook(
            @PathVariable UUID id,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws IOException {

        Student student = authUtil.getAuthenticatedStudent(httpRequest);

        Optional<Ebook> ebookOpt = ebookRepository.findById(id);
        if (ebookOpt.isEmpty()) {
            httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Ebook ebook = ebookOpt.get();
        String safeTitle = ebook.getTitle().replaceAll("[^a-zA-Z0-9._\\- ]", "_");

        httpResponse.setContentType("application/pdf");
        httpResponse.setHeader("Content-Disposition", "inline; filename=\"" + safeTitle + ".pdf\"");
        httpResponse.setHeader("Cache-Control", "private, max-age=300");
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");

        try (ResponseInputStream<GetObjectResponse> s3Stream = r2ImageService.streamFileByUrl(ebook.getUrl())) {
            long contentLength = s3Stream.response().contentLength();
            if (contentLength > 0) {
                httpResponse.setContentLengthLong(contentLength);
            }
            s3Stream.transferTo(httpResponse.getOutputStream());
        }
    }

    @Operation(summary = "Get presigned upload URLs for ebook file and thumbnail")
    @GetMapping("/presigned-upload")
    public ResponseEntity<ApiResponseWithMeta> getPresignedUploadUrl(
            @RequestParam String fileName,
            @RequestParam String contentType,
            @RequestParam String thumbnailName,
            @RequestParam String thumbnailContentType) {

        Map<String, String> ebookUrls = r2ImageService.generatePresignedUploadUrl("ebooks", fileName, contentType);
        Map<String, String> thumbnailUrls = r2ImageService.generatePresignedUploadUrl("ebooks", thumbnailName, thumbnailContentType);

        Map<String, Object> meta = new HashMap<>();
        meta.put("ebookUploadUrl", ebookUrls.get("uploadUrl"));
        meta.put("ebookFileUrl", ebookUrls.get("fileUrl"));
        meta.put("thumbnailUploadUrl", thumbnailUrls.get("uploadUrl"));
        meta.put("thumbnailFileUrl", thumbnailUrls.get("fileUrl"));

        return ResponseEntity.ok(
                new ApiResponseWithMeta("Presigned upload URLs generated successfully", false, meta)
        );
    }

    @Operation(summary = "Save ebook metadata after presigned upload")
    @PostMapping
    public ResponseEntity<Map<String, Object>> uploadEbook(
            @RequestBody EbookAddDto request,
            HttpServletRequest httpRequest) {

        User user = authUtil.getAuthenticatedUser(httpRequest);

        Ebook ebook = ebookService.uploadEbook(request, user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Ebook uploaded successfully");
        response.put("error", false);
        response.put("meta", ebook);

        return ResponseEntity.ok(response);
    }



    @Operation(summary = "Get Ebooks by Course Type ID")
    @GetMapping("/course/{courseTypeId}")
    public ResponseEntity<Map<String, Object>> getEbooksByCourseType(
            @PathVariable UUID courseTypeId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> ebooks = ebookService.getEbooksByCourseType(courseTypeId);
            response.put("meta", ebooks);
            return ResponseEntity.status(HttpStatus.OK).body(ebooks);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Get All Ebooks and optional filter by studentId for check if the student has purchased the ebook or its already in the cart")
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllEbooks(@RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int size,
                                                            @RequestParam(required = false) UUID studentId,
                                                            @RequestParam(required = false) UUID tenantId,
                                                            @RequestParam(required = false) String paymentType) {
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            if (studentId != null) {
                Page<Map<String, Object>> ebooksPage = ebookService.getAllEbooksByStudent(pageable,studentId,paymentType,tenantId);
                response.put("meta", ebooksPage.getContent());
                response.put("currentPage", ebooksPage.getNumber());
                response.put("totalItems", ebooksPage.getTotalElements());
                response.put("totalPages", ebooksPage.getTotalPages());
            } else {
                Page<Ebook> ebooksPage = ebookService.getAllEbooks(pageable,paymentType,tenantId);
                response.put("meta", ebooksPage.getContent());
                response.put("currentPage", ebooksPage.getNumber());
                response.put("totalItems", ebooksPage.getTotalElements());
                response.put("totalPages", ebooksPage.getTotalPages());
            }
            response.put("message", "Ebooks retrieved successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Get All Ebooks and optional filter by studentId for check if the student has purchased the ebook or its already in the cart")
    @GetMapping("/student/all")
    public ResponseEntity<Map<String, Object>> getAllEbooksForStudents(
                                                            @RequestHeader(value = "subdomain", required = true) String subdomain,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int size,
                                                            @RequestParam(required = false) String paymentType,
                                                            HttpServletRequest httpRequest) {
        UUID tenantId = authUtil.validateAndGetUser(subdomain);

        Student student = authUtil.getOptionalAuthenticatedStudent(httpRequest);
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            if (student != null) {
                Page<Map<String, Object>> ebooksPage = ebookService.getAllEbooksByStudent(pageable,student.getId(),paymentType,tenantId);
                response.put("meta", ebooksPage.getContent());
                response.put("currentPage", ebooksPage.getNumber());
                response.put("totalItems", ebooksPage.getTotalElements());
                response.put("totalPages", ebooksPage.getTotalPages());
            } else {
                Page<Ebook> ebooksPage = ebookService.getAllEbooks(pageable,paymentType,tenantId);
                response.put("meta", ebooksPage.getContent());
                response.put("currentPage", ebooksPage.getNumber());
                response.put("totalItems", ebooksPage.getTotalElements());
                response.put("totalPages", ebooksPage.getTotalPages());
            }
            response.put("message", "Ebooks retrieved successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Get All Ebooks By tenantId")
    @GetMapping("/admin/all")
    public ResponseEntity<Map<String, Object>> getAllEbooksForAdmin(@RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int size,
                                                            @RequestParam(required = false) String paymentType,
                                                            HttpServletRequest httpRequest,
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
            Pageable pageable = PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

            Page<Ebook> ebooksPage = ebookService.getAllEbooks(pageable,paymentType,id);
            response.put("meta", ebooksPage.getContent());
            response.put("currentPage", ebooksPage.getNumber());
            response.put("totalItems", ebooksPage.getTotalElements());
            response.put("totalPages", ebooksPage.getTotalPages());

            response.put("message", "Ebooks retrieved successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Get Ebook by ID and optional filter by studentId for check if the student has purchased the ebook or its already in the cart")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEbookById(@PathVariable UUID id,
                                                            @RequestHeader(value = "subdomain", required = true) String subdomain,
                                                            HttpServletRequest httpRequest) {

        UUID tenantId = authUtil.validateAndGetUser(subdomain);
        Student student = authUtil.getOptionalAuthenticatedStudent(httpRequest);

        Map<String, Object> response = new HashMap<>();
        try {
            EbookDetailDto ebook;
            if (student != null) {
                ebook = ebookService.getEbookById(id,tenantId, student.getId());
            } else {
                ebook = ebookService.getEbookById(id,tenantId);
            }
            response.put("meta", ebook);
            response.put("message", "Ebook retrieved successfully");
            response.put("error", false);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Get Ebook by ID for admin or its already in the cart")
    @GetMapping("/admin/{id}")
    public ResponseEntity<Map<String, Object>> getEbookByIdAdmin(@PathVariable UUID id,
                                                            HttpServletRequest httpRequest) {

        User admin = authUtil.getAuthenticatedUser(httpRequest);

        Map<String, Object> response = new HashMap<>();
        try {
            EbookDetailDto ebook;
            ebook = ebookService.getEbookById(id,admin.getId());

            response.put("meta", ebook);
            response.put("message", "Ebook retrieved successfully");
            response.put("error", false);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Update Ebook by ebookId")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEbook(
            @PathVariable UUID id,
            @RequestBody EbookUpdateDto request) {
        try {
            Map<String, Object> response = ebookService.updateEbook(id, request);
            if ((boolean) response.getOrDefault("error", true)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "An unexpected error occurred: " + e.getMessage());
            errorResponse.put("error", true);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @Operation(summary = "Delete Ebook by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteEbook(@PathVariable UUID id) {
        try {
            ebookService.deleteEbook(id);
            ApiResponse response = new ApiResponse("Ebook deleted successfully.", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            ApiResponse response = new ApiResponse(ex.getReason(), true);
            return ResponseEntity.status(ex.getStatusCode()).body(response);
        }
    }

}



    /*@Operation(summary = "Upload an Ebook with optional courseTypeId if you pass it will be saved in the course if not it will not be saved in the course")
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadEbook(
            @RequestParam("file") MultipartFile file,
            @RequestParam("thumbnail") MultipartFile thumbnail,
            @ModelAttribute EbookAddDto request,
            @RequestParam(value = "courseTypeId", required = false) UUID courseTypeId,
            HttpServletRequest httpRequest)  {
        User user = authUtil.getAuthenticatedUser(httpRequest);
        Map<String, Object> response = new HashMap<>();
        try {
            // Validate and set paymentType
            PaymentType paymentType = request.getPaymentType();
            if (paymentType == null) {
                paymentType = PaymentType.FREE; // Default to FREE if not provided
                request.setPaymentType(PaymentType.FREE);
            }
            if (paymentType != PaymentType.FREE && paymentType != PaymentType.PAID) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid paymentType");
            }
            Ebook ebook = new Ebook();
            ebook.setTitle(request.getTitle());
            ebook.setDescription(request.getDescription());
            ebook.setPaymentType(paymentType); // ensure correct paymentType is set
            if(user.getRole() == Role.ADMIN){
                ebook.setCreatedById(user.getId());
                ebook.setTenantId(user.getId());
            }
            else if (user.getRole() == Role.TEACHER) {
                ebook.setCreatedById(user.getId());
                ebook.setTenantId(user.getParent().getId());
            }
            if (request.getPaymentType() == PaymentType.PAID) {
                // For PAID courses, require all fields
                if (    request.getPrice() == null ||
                        request.getDiscountType() == null ||
                        request.getDiscount() == null) {
                    response.put("message", "Price, discountType, and discount are required for PAID courses.");
                    response.put("error", true);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
                Double price = request.getPrice();
                String discountType = request.getDiscountType().toLowerCase();
                Double discount = request.getDiscount();
                // **Validation: Discount should not exceed price if type is "flat"**
                if ("flat".equals(discountType) && discount > price - 1) {
                    response.put("message", "Discount amount cannot be greater or equal than price when discount type is flat.");
                    response.put("error", true);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
                // **Validation: Discount should not exceed 98% if type is "percentage"**
                if ("percentage".equals(discountType) && discount > 98) {
                    response.put("message", "Discount percentage cannot be more than 98%.");
                    response.put("error", true);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
                ebook.setPrice(price);
                ebook.setDiscountType(discountType);
                ebook.setDiscount(discount);
                ebook.setFinalPrice(calculateFinalPrice(price, discount, discountType));
            } else {
                ebook.setPrice(0.0); // Default price if none is provided
                ebook.setFinalPrice(0.0); // You can set the final price accordingly
                ebook.setDiscountType("none");
                ebook.setDiscount(0.0);
            }
//            String fileUrl = googleDriveEbookService.uploadEbook(file);
//            ebook.setUrl(fileUrl);
            ebook.setUrl(null);

            String thumbnailUrl = FileStorageUtil.saveThumbnail(thumbnail);
            ebook.setThumbnailUrl(thumbnailUrl);
            if (courseTypeId != null) {
                ebook.setCourseTypeId(courseTypeId);
            }
            ebookRepository.save(ebook);
            response.put("message", "Ebook uploaded successfully");
            response.put("error", "false");
            response.put("meta", ebook);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            response.put("error", true);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    */
