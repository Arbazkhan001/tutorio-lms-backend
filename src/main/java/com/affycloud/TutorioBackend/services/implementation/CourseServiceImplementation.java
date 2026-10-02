package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.admin.AdminDetailDto;
import com.affycloud.TutorioBackend.dtos.course.*;
import com.affycloud.TutorioBackend.dtos.course.courseType.*;
import com.affycloud.TutorioBackend.dtos.course.courseVariant.CategoryVariantDto;
import com.affycloud.TutorioBackend.dtos.course.courseVariant.CollectionVariantDto;
import com.affycloud.TutorioBackend.dtos.course.courseVariant.CourseVariantDetailDto;
import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.models.*;
import com.affycloud.TutorioBackend.repositories.*;
import com.affycloud.TutorioBackend.services.CourseService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
public class CourseServiceImplementation implements CourseService {

    @Autowired
    private CourseRepository repository;

    @Autowired
    private CourseBatchRepository courseBatchRepository;

    @Autowired
    private CourseTypeRepository courseTypeRepository;

    @Autowired
    private CourseAttemptRepository courseAttemptRepository;

    @Autowired
    private R2ImageServiceImplementation r2ImageService;

    @Autowired
    private CourseBundleRepository courseBundleRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public Map<String, Object> createCourse(CourseAddDto request, MultipartFile image, User user) throws IOException {
        Map<String, Object> response = new HashMap<>();

        // Validate and set paymentType
        PaymentType paymentType = request.getPaymentType();
        if (paymentType == null) {
            paymentType = PaymentType.FREE; // Default to FREE if not provided
            request.setPaymentType(PaymentType.FREE);
        }
        if (paymentType != PaymentType.FREE && paymentType != PaymentType.PAID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid paymentType");
        }

        // Map DTO to Course Entity
        Course course = modelMapper.map(request, Course.class);

        if(user.getRole() == Role.ADMIN){
            course.setCreatedById(user.getId());
            course.setTenantId(user.getId());

        }
        else if (user.getRole() == Role.TEACHER) {
            course.setCreatedById(user.getId());
            course.setTenantId(user.getParent().getId());
        }

        course.setPaymentType(paymentType); // ensure correct paymentType is set

        // Process CourseType and CourseAttempt
        if (request.getCourseTypeDto() != null) {
            CourseType courseType = new CourseType();
            courseType.setDescription(request.getCourseTypeDto().getDescription());
            courseType.setCourseType(request.getCourseTypeDto().getCourseType());
            courseType.setCourse(course);  // Link CourseType to Course

            // Create CourseAttempt from DTO
            CourseAttemptDto courseAttemptDto = request.getCourseTypeDto().getCourseAttemptDto();
            CourseAttempt courseAttempt = new CourseAttempt();

            if (course.getPaymentType() == PaymentType.PAID) {
                // For PAID courses, require all fields
                if (courseAttemptDto == null ||
                        courseAttemptDto.getPrice() == null ||
                        courseAttemptDto.getDiscountType() == null ||
                        courseAttemptDto.getDiscount() == null) {
                    response.put("message", "Price, discountType, and discount are required for PAID courses.");
                    response.put("error", true);
                    return response;
                }

                Double price = courseAttemptDto.getPrice();
                String discountType = courseAttemptDto.getDiscountType().toLowerCase();
                Double discount = courseAttemptDto.getDiscount();

                // **Validation: Discount should not exceed price if type is "flat"**
                if ("flat".equals(discountType) && discount > price - 1) {
                    response.put("message", "Discount amount cannot be greater than  price minus 1 when discount type is flat.");
                    response.put("error", true);
                    return response;
                }

                // **Validation: Discount should not exceed 98% if type is "percentage"**
                if ("percentage".equals(discountType) && discount > 98) {
                    response.put("message", "Discount percentage cannot be more than 98%.");
                    response.put("error", true);
                    return response;
                }

                // Set values in CourseAttempt
                courseAttempt.setAttempt(courseAttemptDto.getAttempt());
                courseAttempt.setExpirationDays(courseAttemptDto.getExpirationDays());
                courseAttempt.setPrice(price);
                courseAttempt.setDiscountType(discountType);
                courseAttempt.setDiscount(discount);
                courseAttempt.setFinalPrice(calculateFinalPrice(price, discount, discountType));

            } else { // FREE course
                    // Allow missing values and set defaults
                courseAttempt.setAttempt(courseAttemptDto != null ? courseAttemptDto.getAttempt() : null);
                courseAttempt.setExpirationDays(courseAttemptDto != null ? courseAttemptDto.getExpirationDays() : null);
                courseAttempt.setPrice(0.0);
                courseAttempt.setDiscountType("none");
                courseAttempt.setDiscount(0.0);
                courseAttempt.setFinalPrice(0.0);
                }

            // Save the CourseAttempt in the CourseType
            courseType.getCourseAttempts().add(courseAttempt);
            course.addVariants(courseType); // Add CourseType to Course
            courseAttempt.setCourseType(courseType);  // Link CourseAttempt to CourseType
        }

        String thumbnailUrl = null;
        if (image != null && !image.isEmpty()) {
            thumbnailUrl = r2ImageService.uploadImage(image);
            course.setThumbnail(thumbnailUrl);
        }
        try {
            repository.save(course);
        } catch (Exception e) {
            response.put("message", "Failed to save Course.");
            response.put("error", true);
            return response;
        }

        response.put("message", "Course created successfully.");
        response.put("error", false);
        return response;
    }

    // Additional helper method for calculating final price (if needed)
    private double calculateFinalPrice(double price, double discount, String discountType) {
        if ("percentage".equalsIgnoreCase(discountType)) {
            return price - (price * discount / 100);
        } else if ("flat".equalsIgnoreCase(discountType)) {
            return price - discount;
        } else if ("none".equalsIgnoreCase(discountType)) {
            return price;
        }
        return price;
    }



    @Override
    public Map<String, Object> addNewType(UUID courseId, CourseTypeDto request) {
        Map<String, Object> response = new HashMap<>();

        // Check if the input data is valid
        if (request == null || request.getCourseAttemptDto() == null) {
            response.put("message", "data is missing or malformed.");
            response.put("error", true);
            return response;
        }

        // Fetch product by productId
        Course course = repository.findById(courseId).orElse(null);
        if (course == null) {
            response.put("message", "Course not found");
            response.put("error", true);
            return response;
        }
            CourseType courseType = new CourseType();
            courseType.setDescription(request.getDescription());
            courseType.setCourseType(request.getCourseType());
            courseType.setCourse(course);  // Link CourseType to Course

            // Create CourseAttempt from DTO
            CourseAttemptDto courseAttemptDto = request.getCourseAttemptDto();
            CourseAttempt courseAttempt = new CourseAttempt();

            if (course.getPaymentType() == PaymentType.PAID) {
                if (courseAttemptDto == null ||
                        courseAttemptDto.getPrice() == null ||
                        courseAttemptDto.getDiscountType() == null ||
                        courseAttemptDto.getDiscount() == null) {
                    response.put("message", "Price, discountType, and discount are required for PAID courses");
                    response.put("error", true);
                    return response;
                }
                // Validate discount rules
                if ("flat".equalsIgnoreCase(courseAttemptDto.getDiscountType()) &&
                        courseAttemptDto.getDiscount() > courseAttemptDto.getPrice()- 1) {
                    response.put("message", "Discount cannot be greater than the price minus 1 for flat discount type.");
                    response.put("error", true);
                    return response;
                }

                if ("percentage".equalsIgnoreCase(courseAttemptDto.getDiscountType()) &&
                        courseAttemptDto.getDiscount() > 98) {
                    response.put("message", "Percentage discount cannot exceed 98%.");
                    response.put("error", true);
                    return response;
                }
                courseAttempt.setPrice(courseAttemptDto.getPrice());
                courseAttempt.setDiscountType(courseAttemptDto.getDiscountType());
                courseAttempt.setDiscount(courseAttemptDto.getDiscount());
                courseAttempt.setFinalPrice(calculateFinalPrice(
                        courseAttemptDto.getPrice(),
                        courseAttemptDto.getDiscount(),
                        courseAttemptDto.getDiscountType()));
            } else {
                // Default values for FREE courses
                courseAttempt.setPrice(0.0);
                courseAttempt.setDiscountType("none");
                courseAttempt.setDiscount(0.0);
                courseAttempt.setFinalPrice(0.0);
            }
            courseAttempt.setAttempt(courseAttemptDto.getAttempt());
            courseAttempt.setExpirationDays(courseAttemptDto.getExpirationDays());
            courseAttempt.setCourseType(courseType);  // Link CourseAttempt to CourseType
            // Save the CourseAttempt in the CourseType
            courseType.getCourseAttempts().add(courseAttempt);
            course.addVariants(courseType); // Add CourseType to Course


        // Save updated product
        try {
            repository.save(course);
        } catch (Exception e) {
            response.put("message", "Failed to add new course-type");
            response.put("error", true);
            return response;
        }

        response.put("message", "Course Type added successfully.");
        response.put("error", false);
        return response;
    }


    @Override
    public Map<String, Object> createCourseAttempt(UUID courseTypeId, CourseAttemptDto courseAttemptDto) {
        Map<String, Object> response = new HashMap<>();

        // Retrieve the existing CourseType
        Optional<CourseType> courseTypeOptional = courseTypeRepository.findById(courseTypeId);
        if (!courseTypeOptional.isPresent()) {
            response.put("message", "CourseType not found");
            response.put("error", true);
            return response;
        }
        CourseType courseType = courseTypeOptional.get();

        // Create a new CourseAttempt and map the DTO values
        CourseAttempt courseAttempt = new CourseAttempt();
        if (courseType.getCourse().getPaymentType() == PaymentType.PAID) {
            if (courseAttemptDto == null ||
                    courseAttemptDto.getPrice() == null ||
                    courseAttemptDto.getDiscountType() == null ||
                    courseAttemptDto.getDiscount() == null) {
                response.put("message", "Price, discountType, and discount are required for PAID courses");
                response.put("error", true);
                return response;
            }
            // Validate discount rules
            if ("flat".equalsIgnoreCase(courseAttemptDto.getDiscountType()) &&
                    courseAttemptDto.getDiscount() > courseAttemptDto.getPrice()- 1) {
                response.put("message", "Discount amount cannot be greater than  price minus 1 when discount type is flat.");
                response.put("error", true);
                return response;
            }

            if ("percentage".equalsIgnoreCase(courseAttemptDto.getDiscountType()) &&
                    courseAttemptDto.getDiscount() > 98) {
                response.put("message", "Percentage discount cannot exceed 98%.");
                response.put("error", true);
                return response;
            }

            courseAttempt.setPrice(courseAttemptDto.getPrice());
            courseAttempt.setDiscountType(courseAttemptDto.getDiscountType());
            courseAttempt.setDiscount(courseAttemptDto.getDiscount());
            courseAttempt.setFinalPrice(calculateFinalPrice(
                    courseAttemptDto.getPrice(),
                    courseAttemptDto.getDiscount(),
                    courseAttemptDto.getDiscountType()));
        } else {
            // Default values for FREE courses
            courseAttempt.setPrice(0.0);
            courseAttempt.setDiscountType("none");
            courseAttempt.setDiscount(0.0);
            courseAttempt.setFinalPrice(0.0);
        }
        courseAttempt.setAttempt(courseAttemptDto.getAttempt());
        courseAttempt.setPlan(courseAttemptDto.getPlan());
        courseAttempt.setHasBatch(courseAttemptDto.getHasBatch());
        courseAttempt.setExpirationDays(courseAttemptDto.getExpirationDays());
        // Associate the course attempt with the course type using helper method
        courseType.addVariants(courseAttempt);

        // Persist the update. Cascade settings will ensure the attempt is saved.
        try {
            courseTypeRepository.save(courseType);
        } catch (Exception e) {
            response.put("message", "Failed to add course attempt: " + e.getMessage());
            response.put("error", true);
            return response;
        }

        response.put("message", "Course Attempt added successfully.");
        response.put("error", false);
        return response;
    }


    @Override
    public ApiResponse  createCourseBatch(UUID courseAttemptId, CourseBatchDto courseBatchDto) {
        CourseAttempt courseAttempt = courseAttemptRepository.findById(courseAttemptId)
                .orElseThrow(() -> new RuntimeException("Course Attempt not found"));

        CourseBatch courseBatch = new CourseBatch();
        courseBatch.setBatch(courseBatchDto.getBatch());
        courseBatch.setStudentLimit(courseBatchDto.getStudentLimit());

        courseAttempt.addVariants(courseBatch);   // rename helper method properly

        courseAttemptRepository.save(courseAttempt);

        return new ApiResponse("Batch added successfully.", false);
    }


    @Override
    public ApiResponse updateCourseBatch(UUID courseBatchId, CourseBatchUpdateDto dto) {
        CourseBatch existingBatch = courseBatchRepository.findById(courseBatchId)
                .orElseThrow(() -> new RuntimeException("Batch not found"));

        if (dto.getBatch() != null && !dto.getBatch().isBlank()) {
            existingBatch.setBatch(dto.getBatch());
        }

        if (dto.getStudentLimit() != null && dto.getStudentLimit() > 0) {
            existingBatch.setStudentLimit(dto.getStudentLimit());
        }

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            existingBatch.setTitle(dto.getTitle());
        }

        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            existingBatch.setDescription(dto.getDescription());
        }

        if (dto.getUrl() != null && !dto.getUrl().isBlank()) {
            existingBatch.setUrl(dto.getUrl());
        }

        courseBatchRepository.save(existingBatch);

        return new ApiResponse("Batch updated successfully.", false);
    }



    @Override
    public Map<String, Object> updateCourseType(UUID courseId, UUID courseTypeId, CourseTypeUpdateDto courseTypeUpdateDto) {
        Map<String, Object> response = new HashMap<>();

        // Check if the input data is valid
        if (courseTypeUpdateDto == null) {
            response.put("message", "data is missing or malformed.");
            response.put("error", true);
            return response;
        }

        // Fetch the course by courseId
        Course course = repository.findById(courseId).orElse(null);
        if (course == null) {
            response.put("message", "Course not found");
            response.put("error", true);
            return response;
        }

        // Fetch the courseType by courseTypeId
        // Find the CourseType by courseTypeId within the course's variants
        CourseType courseType = course.getCourseTypes().stream()
                .filter(v -> v.getId().equals(courseTypeId))
                .findFirst()
                .orElse(null);

        if (courseType == null) {
            response.put("message", "Course Type not found");
            response.put("error", true);
            return response;
        }
        // Update the existing CourseType with the new data
        courseType.setDescription(courseTypeUpdateDto.getDescription());

        // Save the updated course
        try {
            repository.save(course);
            response.put("message", "Course Type updated successfully.");
            response.put("error", false);
            return response;
        } catch (Exception e) {
            response.put("message", "Failed to update course-type");
            response.put("error", true);
            return response;

        }
    }

    @Override
    public Map<String, Object> updateCourseTypeWithAttempt(UUID courseId, UUID courseTypeId,UUID courseAttemptId, CourseTypeUpdateDto courseTypeUpdateDto) {
        Map<String, Object> response = new HashMap<>();

        // Check if the input data is valid
        if (courseTypeUpdateDto == null) {
            response.put("message", "data is missing or malformed.");
            response.put("error", true);
            return response;
        }

        // Fetch the course by courseId
        Course course = repository.findById(courseId).orElse(null);
        if (course == null) {
            response.put("message", "Course not found");
            response.put("error", true);
            return response;
        }

        // Fetch the courseType by courseTypeId
        // Find the CourseType by courseTypeId within the course's variants
        CourseType courseType = course.getCourseTypes().stream()
                .filter(v -> v.getId().equals(courseTypeId))
                .findFirst()
                .orElse(null);

        if (courseType == null) {
            response.put("message", "Course Type not found");
            response.put("error", true);
            return response;
        }


        CourseAttempt courseAttempt = courseType.getCourseAttempts().stream()
                .filter(v -> v.getId().equals(courseAttemptId))
                .findFirst()
                .orElse(null);

        if (courseAttempt == null) {
            response.put("message", "Course Attempt not found");
            response.put("error", true);
            return response;
        }

        // Update the existing CourseType with the new data
        courseType.setDescription(courseTypeUpdateDto.getDescription());
        courseType.setCourseType(courseTypeUpdateDto.getCourseType());
        // Extract CourseAttemptDto
        CourseAttemptDto attemptDto = courseTypeUpdateDto.getCourseAttemptDto();

        if (course.getPaymentType() == PaymentType.PAID) {
            if (courseTypeUpdateDto.getCourseAttemptDto() == null ||
                    courseTypeUpdateDto.getCourseAttemptDto().getPrice() == null ||
                    courseTypeUpdateDto.getCourseAttemptDto().getDiscountType() == null ||
                    courseTypeUpdateDto.getCourseAttemptDto().getDiscount() == null) {
                response.put("message", "Price, discountType, and discount are required for PAID courses");
                response.put("error", true);
                return response;
            }
            Double price = courseTypeUpdateDto.getCourseAttemptDto().getPrice();
            String discountType = courseTypeUpdateDto.getCourseAttemptDto().getDiscountType().toLowerCase();
            Double discount = courseTypeUpdateDto.getCourseAttemptDto().getDiscount();


            // **Validation: Discount should not exceed price if type is "flat"**
            if ("flat".equals(discountType) && discount > price- 1) {
                response.put("message", "Discount amount cannot be greater than  price minus 1 when discount type is flat.");
                response.put("error", true);
                return response;
            }

            // **Validation: Discount should not exceed 98% if type is "percentage"**
            if ("percentage".equals(discountType) && discount > 98) {
                response.put("message", "Discount percentage cannot be more than 98%.");
                response.put("error", true);
                return response;
            }

            // Set values in CourseAttempt
            courseAttempt.setAttempt(courseTypeUpdateDto.getCourseAttemptDto().getAttempt());
            courseAttempt.setExpirationDays(courseTypeUpdateDto.getCourseAttemptDto().getExpirationDays());
            courseAttempt.setPrice(price);
            courseAttempt.setDiscountType(discountType);
            courseAttempt.setDiscount(discount);
            courseAttempt.setFinalPrice(calculateFinalPrice(price, discount, discountType));

        } else { // FREE course
            courseAttempt.setAttempt(courseTypeUpdateDto.getCourseAttemptDto() != null ? courseTypeUpdateDto.getCourseAttemptDto().getAttempt() : null);
            courseAttempt.setExpirationDays(courseTypeUpdateDto.getCourseAttemptDto().getExpirationDays());
            courseAttempt.setPrice(0.0);
            courseAttempt.setDiscountType("none");
            courseAttempt.setDiscount(0.0);
            courseAttempt.setFinalPrice(0.0);
        }

        courseAttempt.setCourseType(courseType);  // Link CourseAttempt to CourseType
        courseAttempt.setPlan(courseTypeUpdateDto.getCourseAttemptDto().getPlan());  // Link CourseAttempt to CourseType
        courseAttempt.setHasBatch(courseTypeUpdateDto.getCourseAttemptDto().getHasBatch());
        // Save the updated course
        try {
            repository.save(course);
        } catch (Exception e) {
            response.put("message", "Failed to update course-type");
            response.put("error", true);
            return response;
        }

        response.put("message", "Course Type updated successfully.");
        response.put("error", false);
        return response;
    }


    @Override
    public Map<String, Object> updateCourse(UUID courseId, CourseUpdateDto request, MultipartFile image) throws IOException {

        Map<String, Object> response = new HashMap<>();

        // Find the course by ID
        Optional<Course> courseOptional = repository.findById(courseId);
        if (courseOptional.isEmpty()) {
            response.put("message", "Course not found.");
            response.put("error", true);
            return response;
        }

        Course course = courseOptional.get();

        // Update the title
        if (request.getTitle() != null) {
            course.setTitle(request.getTitle());
        }

        if (image != null && !image.isEmpty()) {
            // delete old thumbnail
            r2ImageService.deleteFileByUrl(course.getThumbnail());
            // upload new one
            String imageUrl = r2ImageService.uploadImage(image);
            course.setThumbnail(imageUrl);
        }

        try {
            repository.save(course);
        } catch (Exception e) {
            response.put("message", "Failed to update course.");
            response.put("error", true);
            return response;
        }

        response.put("message", "Course updated successfully.");
        response.put("error", false);
        return response;
    }

    /**
     * Builds subject -> chapter -> video summaries for a course type using 3 bulk queries
     * (subjects, chapters, videos) instead of walking lazy collections one entity at a time,
     * which previously fired a query per subject and per chapter.
     */
    private List<SubjectSummaryDto> mapSubjectsSummary(UUID courseTypeId) {
        Sort createdDesc = Sort.by(Sort.Direction.DESC, "createdAt");

        List<Subject> subjects = subjectRepository.findByCourseTypeIdAndIsActiveTrue(courseTypeId, createdDesc);
        if (subjects.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> subjectIds = subjects.stream().map(Subject::getId).collect(Collectors.toList());

        Map<UUID, List<Chapter>> chaptersBySubjectId = chapterRepository
                .findBySubjectIdInAndIsActiveTrue(subjectIds, createdDesc).stream()
                .collect(Collectors.groupingBy(chapter -> chapter.getSubject().getId()));

        Map<UUID, List<Video>> videosByChapterId = videoRepository
                .findByCourseTypeId(courseTypeId, createdDesc).stream()
                .collect(Collectors.groupingBy(video -> video.getChapter().getId()));

        List<SubjectSummaryDto> result = new ArrayList<>();
        for (Subject subject : subjects) {
            List<ChapterSummaryDto> chapterDtos = new ArrayList<>();

            for (Chapter chapter : chaptersBySubjectId.getOrDefault(subject.getId(), Collections.emptyList())) {
                List<Video> videos = videosByChapterId.get(chapter.getId());
                if (videos == null || videos.isEmpty()) {
                    continue;
                }

                ChapterSummaryDto chapterDto = new ChapterSummaryDto();
                chapterDto.setId(chapter.getId());
                chapterDto.setName(chapter.getName());
                chapterDto.setVideos(videos.stream()
                        .map(video -> {
                            VideoSummaryDto videoDto = new VideoSummaryDto();
                            videoDto.setId(video.getId());
                            videoDto.setTitle(video.getTitle());
                            return videoDto;
                        })
                        .collect(Collectors.toList()));
                chapterDtos.add(chapterDto);
            }

            if (chapterDtos.isEmpty()) {
                continue;
            }

            SubjectSummaryDto subjectDto = new SubjectSummaryDto();
            subjectDto.setId(subject.getId());
            subjectDto.setName(subject.getName());
            subjectDto.setChapters(chapterDtos);
            result.add(subjectDto);
        }

        return result;
    }

    @Override
    public CourseDetailDto getCourseById(UUID id, UUID tenantId) {
        Course course = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found with ID: " + id));
        if (!Objects.equals(course.getTenantId(), tenantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found.");
        }
        // Map Course entity to CourseDetailDto
        CourseDetailDto courseDetailDto = modelMapper.map(course, CourseDetailDto.class);

        // Now map courseTypes to courseTypeDetailDtos
        List<CourseTypeDetailDto> courseTypeDetailDtos = course.getCourseTypes().stream()
                .map(courseType -> {

                    CourseTypeDetailDto courseTypeDetailDto =
                            modelMapper.map(courseType, CourseTypeDetailDto.class);

                    List<CourseAttemptDetailDto> courseAttemptDtos =
                            courseType.getCourseAttempts().stream()
                                    .filter(attempt -> attempt.getIsActive() != null && attempt.getIsActive())
                                    .map(courseAttempt -> {

                                        CourseAttemptDetailDto attemptDto =
                                                modelMapper.map(courseAttempt, CourseAttemptDetailDto.class);

                                        // 🔥 Fix: Map batches
                                        List<CourseBatchDetailDto> batchDtos =
                                                courseAttempt.getCourseBatches().stream()
                                                        .filter(batch -> batch.getIsActive() != null && batch.getIsActive())
                                                        .map(batch -> modelMapper.map(batch, CourseBatchDetailDto.class))
                                                        .collect(Collectors.toList());

                                        attemptDto.setCourseBatch(batchDtos);

                                        return attemptDto;
                                    })
                                    .collect(Collectors.toList());

                    courseTypeDetailDto.setCourseAttempts(courseAttemptDtos);
                    courseTypeDetailDto.setSubjects(mapSubjectsSummary(courseType.getId()));

                    return courseTypeDetailDto;
                })
                .collect(Collectors.toList());

        courseDetailDto.setCourseTypes(courseTypeDetailDtos);

        return courseDetailDto;
    }

    @Override
    public CourseDetailDto getCourseByIdForAdmin(UUID id, UUID tenantId) {
        Course course = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found with ID: " + id));
        if (!Objects.equals(course.getTenantId(), tenantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found.");
        }
        // Map Course entity to CourseDetailDto
        CourseDetailDto courseDetailDto = modelMapper.map(course, CourseDetailDto.class);

        // Now map courseTypes to courseTypeDetailDtos
        List<CourseTypeDetailDto> courseTypeDetailDtos = course.getCourseTypes().stream()
                .map(courseType -> {

                    CourseTypeDetailDto courseTypeDetailDto =
                            modelMapper.map(courseType, CourseTypeDetailDto.class);

                    List<CourseAttemptDetailDto> courseAttemptDtos =
                            courseType.getCourseAttempts().stream()
                                    .map(courseAttempt -> {

                                        CourseAttemptDetailDto attemptDto =
                                                modelMapper.map(courseAttempt, CourseAttemptDetailDto.class);

                                        // 🔥 Fix: Map batches
                                        List<CourseBatchDetailDto> batchDtos =
                                                courseAttempt.getCourseBatches().stream()
                                                        .map(batch -> modelMapper.map(batch, CourseBatchDetailDto.class))
                                                        .collect(Collectors.toList());

                                        attemptDto.setCourseBatch(batchDtos);

                                        return attemptDto;
                                    })
                                    .collect(Collectors.toList());

                    courseTypeDetailDto.setCourseAttempts(courseAttemptDtos);

                    return courseTypeDetailDto;
                })
                .collect(Collectors.toList());

        courseDetailDto.setCourseTypes(courseTypeDetailDtos);

        return courseDetailDto;
    }

    @Override
    public List<CourseBatchDetailDto> getCourseBatchByAttemptId(UUID attemptId) {

        CourseAttempt courseAttempt = courseAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course Attempt not found with ID: " + attemptId));

        List<CourseBatch> courseBatches = courseBatchRepository.findAllByCourseAttempt(courseAttempt, Sort.by("createdAt").descending());

        if (courseBatches.isEmpty()) {
            return Collections.emptyList();
        }

        return courseBatches.stream()
                .map(courseBatch
                        -> modelMapper.map(courseBatch, CourseBatchDetailDto.class))
                .collect(Collectors.toList());

    }

    @Override
    @Transactional(readOnly = true)
    public CourseDetailDto getCourseById(UUID id, UUID tenantId, UUID studentId) {

        Course course = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Course not found with ID: " + id));

        if (!Objects.equals(course.getTenantId(), tenantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found.");
        }

        studentRepository.findById(studentId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Student not found with ID: " + studentId));

        // Fetch cart + enrollment info
        Set<UUID> courseIdsInCart = cartItemRepository.findCourseIdsByStudentId(studentId);
        Set<UUID> enrolledCourseIds = enrollmentRepository.findCourseIdsByStudentId(studentId);

        CourseDetailDto courseDetailDto = modelMapper.map(course, CourseDetailDto.class);

        List<CourseTypeDetailDto> courseTypeDetailDtos = course.getCourseTypes().stream()
                .map(courseType -> {

                    CourseTypeDetailDto typeDto =
                            modelMapper.map(courseType, CourseTypeDetailDto.class);

                    List<CourseAttemptDetailDto> attemptDtos =
                            courseType.getCourseAttempts().stream()
                                    .filter(attempt -> attempt.getIsActive() != null && attempt.getIsActive())
                                    .map(attempt -> {

                                        CourseAttemptDetailDto attemptDto =
                                                modelMapper.map(attempt, CourseAttemptDetailDto.class);

                                        // 🔥 IMPORTANT FIX: Map batches
                                        List<CourseBatchDetailDto> batchDtos =
                                                attempt.getCourseBatches().stream()
                                                        .filter(batch -> batch.getIsActive() != null && batch.getIsActive())
                                                        .map(batch ->
                                                                modelMapper.map(batch,
                                                                        CourseBatchDetailDto.class))
                                                        .collect(Collectors.toList());

                                        attemptDto.setCourseBatch(batchDtos);

                                        return attemptDto;
                                    })
                                    .collect(Collectors.toList());

                    typeDto.setCourseAttempts(attemptDtos);
                    typeDto.setSubjects(mapSubjectsSummary(courseType.getId()));

                    return typeDto;
                })
                .collect(Collectors.toList());

        courseDetailDto.setCourseTypes(courseTypeDetailDtos);

        // Cart + Enrollment flags
        courseDetailDto.setInCart(courseIdsInCart.contains(course.getId()));
        courseDetailDto.setInMyLearning(enrolledCourseIds.contains(course.getId()));

        // Attempt in cart
        List<UUID> attemptIdsInCart =
                cartItemRepository.findAttemptIdsByStudentIdAndCourseId(studentId, course.getId());

        if (!attemptIdsInCart.isEmpty()) {
            courseDetailDto.setAttemptInTheCart(attemptIdsInCart.get(0));
        }

        return courseDetailDto;
    }

    @Override
    public List<CourseDetailDto> getAllCourses() {
        List<Course> courses = repository.findAll();
        if (courses.isEmpty()) {
            return Collections.emptyList();
        }

        return courses.stream()
                .map(course -> {
                    CourseDetailDto courseDetailDto = modelMapper.map(course, CourseDetailDto.class);

                    // Map courseTypes to courseTypeDetailDtos and handle course attempts
                    List<CourseTypeDetailDto> courseTypeDetailDtos = course.getCourseTypes().stream()
                            .map(courseType -> {
                                // Map courseType to CourseTypeDetailDto
                                CourseTypeDetailDto courseTypeDetailDto = modelMapper.map(courseType, CourseTypeDetailDto.class);

                                // Map CourseAttempts to CourseAttemptDto
                                List<CourseAttemptDetailDto> courseAttemptDtos = courseType.getCourseAttempts().stream()
                                        .map(courseAttempt -> modelMapper.map(courseAttempt, CourseAttemptDetailDto.class))
                                        .collect(Collectors.toList());

                                // Set the courseAttemptDto in the courseTypeDetailDto
                                courseTypeDetailDto.setCourseAttempts(courseAttemptDtos.isEmpty() ? null : courseAttemptDtos);  // Set first attempt if available

                                return courseTypeDetailDto;
                            })
                            .collect(Collectors.toList());

                    courseDetailDto.setCourseTypes(courseTypeDetailDtos);
                    return courseDetailDto;
                })
                .collect(Collectors.toList());
    }


    @Override
    public Page<CourseDetailDto> getCoursesByFilters(String category, String collection, String subject, PaymentType paymentType, UUID tenantId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> courses = repository.findByFilters2(category, collection, subject, paymentType, tenantId, pageable);

        List<CourseDetailDto> courseDtos = courses.stream()
                .map(course -> {
                    CourseDetailDto courseDetailDto = modelMapper.map(course, CourseDetailDto.class);

                    // Map courseTypes to courseTypeDetailDtos and handle course attempts
                    List<CourseTypeDetailDto> courseTypeDetailDtos = course.getCourseTypes().stream()
                            .map(courseType -> {
                                CourseTypeDetailDto courseTypeDetailDto = modelMapper.map(courseType, CourseTypeDetailDto.class);

                                List<CourseAttemptDetailDto> courseAttemptDtos = courseType.getCourseAttempts().stream()
                                        .filter(attempt -> attempt.getIsActive() != null && attempt.getIsActive())
                                        .map(courseAttempt -> {
                                            CourseAttemptDetailDto attemptDto = modelMapper.map(courseAttempt, CourseAttemptDetailDto.class);
                                            if (courseAttempt.getCourseBatches() != null) {
                                                List<CourseBatchDetailDto> batchDtos = courseAttempt.getCourseBatches().stream()
                                                    .filter(batch -> batch.getIsActive() != null && batch.getIsActive())
                                                    .map(batch -> modelMapper.map(batch, CourseBatchDetailDto.class))
                                                    .collect(Collectors.toList());
                                                attemptDto.setCourseBatch(batchDtos);
                                            }
                                            return attemptDto;
                                        })
                                        .collect(Collectors.toList());

                                courseTypeDetailDto.setCourseAttempts(courseAttemptDtos.isEmpty() ? null : courseAttemptDtos);

                                return courseTypeDetailDto;
                            })
                            .collect(Collectors.toList());

                    courseDetailDto.setCourseTypes(courseTypeDetailDtos);
                    return courseDetailDto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(courseDtos, pageable, courses.getTotalElements());
    }


    @Override
    public Page<CourseDetailDto> getCoursesByFiltersForAdmin(String category, String collection, String subject, PaymentType paymentType, int page, int size,UUID id) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Course> courses = repository.findByFilters3(category, collection, subject, paymentType, id,pageable);

        List<CourseDetailDto> courseDtos = courses.stream()
                .map(course -> {
                    CourseDetailDto courseDetailDto = modelMapper.map(course, CourseDetailDto.class);

                    // Map courseTypes to courseTypeDetailDtos and handle course attempts
                    List<CourseTypeDetailDto> courseTypeDetailDtos = course.getCourseTypes().stream()
                            .map(courseType -> {
                                CourseTypeDetailDto courseTypeDetailDto = modelMapper.map(courseType, CourseTypeDetailDto.class);

                                List<CourseAttemptDetailDto> courseAttemptDtos = courseType.getCourseAttempts().stream()
                                        .map(courseAttempt -> modelMapper.map(courseAttempt, CourseAttemptDetailDto.class))
                                        .collect(Collectors.toList());

                                courseTypeDetailDto.setCourseAttempts(courseAttemptDtos.isEmpty() ? null : courseAttemptDtos);

                                return courseTypeDetailDto;
                            })
                            .collect(Collectors.toList());

                    courseDetailDto.setCourseTypes(courseTypeDetailDtos);
                    return courseDetailDto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(courseDtos, pageable, courses.getTotalElements());
    }

    @Override
    public List<CourseDetailDto> getCoursesByFiltersWithTenant(String category, String collection, String subject, PaymentType paymentType,UUID id) {

        List<Course> courses = repository.findByFilters4(category,
                collection, subject, paymentType, id, Sort.by("createdAt").descending());

        return courses.stream()
                .map(course -> {
                    CourseDetailDto courseDetailDto = modelMapper.map(course, CourseDetailDto.class);

                    // Map courseTypes to courseTypeDetailDtos and handle course attempts
                    List<CourseTypeDetailDto> courseTypeDetailDtos = course.getCourseTypes().stream()
                            .map(courseType -> {
                                CourseTypeDetailDto courseTypeDetailDto = modelMapper.map(courseType, CourseTypeDetailDto.class);

                                List<CourseAttemptDetailDto> courseAttemptDtos = courseType.getCourseAttempts().stream()
                                        .filter(attempt -> attempt.getIsActive() != null && attempt.getIsActive())
                                        .map(courseAttempt -> {
                                            CourseAttemptDetailDto attemptDto = modelMapper.map(courseAttempt, CourseAttemptDetailDto.class);
                                            if (courseAttempt.getCourseBatches() != null) {
                                                List<CourseBatchDetailDto> batchDtos = courseAttempt.getCourseBatches().stream()
                                                    .filter(batch -> batch.getIsActive() != null && batch.getIsActive())
                                                    .map(batch -> modelMapper.map(batch, CourseBatchDetailDto.class))
                                                    .collect(Collectors.toList());
                                                attemptDto.setCourseBatch(batchDtos);
                                            }
                                            return attemptDto;
                                        })
                                        .collect(Collectors.toList());

                                courseTypeDetailDto.setCourseAttempts(courseAttemptDtos.isEmpty() ? null : courseAttemptDtos);

                                return courseTypeDetailDto;
                            })
                            .collect(Collectors.toList());

                    courseDetailDto.setCourseTypes(courseTypeDetailDtos);
                    return courseDetailDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<CourseDetailDto> getCoursesByFiltersByStudent(String category, String collection, String subject, PaymentType paymentType, UUID studentId, UUID tenantId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Course> courses = repository.findByFilters2(category, collection, subject, paymentType, tenantId, pageable);

        Set<UUID> enrolledCourseIds = enrollmentRepository.findCourseIdsByStudentId(studentId);

        List<CourseDetailDto> courseDtos = courses.stream()
                .map(course -> {
                    CourseDetailDto courseDetailDto = modelMapper.map(course, CourseDetailDto.class);

                    // Map courseTypes to courseTypeDetailDtos and handle course attempts
                    List<CourseTypeDetailDto> courseTypeDetailDtos = course.getCourseTypes().stream()
                            .map(courseType -> {
                                CourseTypeDetailDto courseTypeDetailDto = modelMapper.map(courseType, CourseTypeDetailDto.class);

                                List<CourseAttemptDetailDto> courseAttemptDtos = courseType.getCourseAttempts().stream()
                                        .filter(attempt -> attempt.getIsActive() != null && attempt.getIsActive())
                                        .map(courseAttempt -> {
                                            CourseAttemptDetailDto attemptDto = modelMapper.map(courseAttempt, CourseAttemptDetailDto.class);
                                            if (courseAttempt.getCourseBatches() != null) {
                                                List<CourseBatchDetailDto> batchDtos = courseAttempt.getCourseBatches().stream()
                                                    .filter(batch -> batch.getIsActive() != null && batch.getIsActive())
                                                    .map(batch -> modelMapper.map(batch, CourseBatchDetailDto.class))
                                                    .collect(Collectors.toList());
                                                attemptDto.setCourseBatch(batchDtos);
                                            }
                                            return attemptDto;
                                        })
                                        .collect(Collectors.toList());

                                courseTypeDetailDto.setCourseAttempts(courseAttemptDtos.isEmpty() ? null : courseAttemptDtos);

                                return courseTypeDetailDto;
                            })
                            .collect(Collectors.toList());

                    courseDetailDto.setCourseTypes(courseTypeDetailDtos);

                    // Set inCart and inMyLearning fields
//                    courseDetailDto.setInCart(courseIdsInCart.contains(course.getId()));
                    courseDetailDto.setInMyLearning(enrolledCourseIds.contains(course.getId()));

                    return courseDetailDto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(courseDtos, pageable, courses.getTotalElements());
    }

    @Override
    public Page<CourseDetailDto>  getAllActiveCourses(int page, int size, UUID id) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//        Page<Course> courses = repository.findAll(pageable);
        Page<Course> courses = repository.findAllByTenantId(id,pageable);

        List<CourseDetailDto> courseDtos = courses.stream()
                .map(course -> {
                    CourseDetailDto courseDetailDto = modelMapper.map(course, CourseDetailDto.class);

                    // Map courseTypes to courseTypeDetailDtos and handle course attempts
                    List<CourseTypeDetailDto> courseTypeDetailDtos = course.getCourseTypes().stream()
                            .map(courseType -> {
                                CourseTypeDetailDto courseTypeDetailDto = modelMapper.map(courseType, CourseTypeDetailDto.class);

                                List<CourseAttemptDetailDto> courseAttemptDtos = courseType.getCourseAttempts().stream()
                                        .map(courseAttempt -> modelMapper.map(courseAttempt, CourseAttemptDetailDto.class))
                                        .collect(Collectors.toList());

                                courseTypeDetailDto.setCourseAttempts(courseAttemptDtos.isEmpty() ? null : courseAttemptDtos);

                                return courseTypeDetailDto;
                            })
                            .collect(Collectors.toList());

                    courseDetailDto.setCourseTypes(courseTypeDetailDtos);
                    return courseDetailDto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(courseDtos, pageable, courses.getTotalElements());
    }

    @Override
    public Page<CourseDetailDto> getAllActiveCourses2(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Course> courses = repository.findAllByIsActiveTrue(pageable);

        List<CourseDetailDto> courseDtos = courses.stream()
                .map(course -> {
                    CourseDetailDto courseDetailDto = modelMapper.map(course, CourseDetailDto.class);

                    // Map courseTypes to courseTypeDetailDtos and handle course attempts
                    List<CourseTypeDetailDto> courseTypeDetailDtos = course.getCourseTypes().stream()
                            .map(courseType -> {
                                CourseTypeDetailDto courseTypeDetailDto = modelMapper.map(courseType, CourseTypeDetailDto.class);

                                List<CourseAttemptDetailDto> courseAttemptDtos = courseType.getCourseAttempts().stream()
                                        .map(courseAttempt -> modelMapper.map(courseAttempt, CourseAttemptDetailDto.class))
                                        .collect(Collectors.toList());

                                courseTypeDetailDto.setCourseAttempts(courseAttemptDtos.isEmpty() ? null : courseAttemptDtos);

                                return courseTypeDetailDto;
                            })
                            .collect(Collectors.toList());

                    courseDetailDto.setCourseTypes(courseTypeDetailDtos);
                    return courseDetailDto;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(courseDtos, pageable, courses.getTotalElements());
    }

    @Override
    public Page<CourseDetailDto> getAllActiveFeaturedCourses(Pageable pageable, UUID tenantId) {

        Page<Course> courses =
                repository.findAllByIsActiveTrueAndPaymentTypeAndTenantId(
                        pageable, PaymentType.PAID, tenantId);

        return mapToCourseDetailPage(courses, pageable);
    }


    @Override
    public Page<CourseDetailDto> getAllPopularCourses(Pageable pageable, UUID tenantId) {

        Page<Course> courses =
                repository.findAllByIsActiveTrueAndPaymentTypeAndIsPopularTrueAndTenantId(
                        pageable, PaymentType.PAID, tenantId);

        return mapToCourseDetailPage(courses, pageable);
    }


    @Override
    public Page<CourseDetailDto> getAllBannerCourses(Pageable pageable, UUID tenantId) {

        Page<Course> courses =
                repository.findAllByIsActiveTrueAndPaymentTypeAndOnBannerTrueAndTenantId(
                        pageable, PaymentType.PAID, tenantId);

        return mapToCourseDetailPage(courses, pageable);
    }

    private Page<CourseDetailDto> mapToCourseDetailPage(Page<Course> courses, Pageable pageable) {

        List<CourseDetailDto> courseDtos = courses.stream()
                .map(course -> {
                    CourseDetailDto dto = modelMapper.map(course, CourseDetailDto.class);

                    List<CourseTypeDetailDto> courseTypes = course.getCourseTypes().stream()
                            .map(courseType -> {
                                CourseTypeDetailDto typeDto =
                                        modelMapper.map(courseType, CourseTypeDetailDto.class);

                                List<CourseAttemptDetailDto> attempts =
                                        courseType.getCourseAttempts().stream()
                                                .map(a -> modelMapper.map(a, CourseAttemptDetailDto.class))
                                                .toList();

                                typeDto.setCourseAttempts(attempts.isEmpty() ? null : attempts);
                                return typeDto;
                            }).toList();

                    dto.setCourseTypes(courseTypes);
                    return dto;
                })
                .toList();

        return new PageImpl<>(courseDtos, pageable, courses.getTotalElements());
    }



    @Override
    public List<CourseDetailWithoutCourseTypeDto> getCoursesByFiltersWithoutCourseType(String category, String collection, String subject) {
        List<Course> courses = repository.findByFilters(category, collection, subject);
        if (courses.isEmpty()) {
            return Collections.emptyList();
        }

        return courses.stream()
                .map(course -> {
                    CourseDetailWithoutCourseTypeDto dto = modelMapper.map(course, CourseDetailWithoutCourseTypeDto.class);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDetailWithoutCourseTypeDto> getAllCoursesWithoutCourseType() {
        List<Course> courses = repository.findAll();
        return courses.stream()
                .map(course -> {
                    CourseDetailWithoutCourseTypeDto dto = modelMapper.map(course, CourseDetailWithoutCourseTypeDto.class);
                    return dto;

                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryVariantDto> getAllCoursesVariant(UUID tenantId) {

        List<Course> courses = repository.findAllByTenantId(tenantId);
        if (courses.isEmpty()) {
            return new ArrayList<>();
        }

        List<CourseVariantDetailDto> variants = courses.stream()
                .map(course -> modelMapper.map(course, CourseVariantDetailDto.class))
                .filter(v ->
                        isValid(v.getCategory()) &&
                                isValid(v.getCollection()) &&
                                isValid(v.getSubject())
                )
                .collect(Collectors.toList());

        Map<String, Map<String, List<String>>> grouped = variants.stream()
                .collect(Collectors.groupingBy(
                        CourseVariantDetailDto::getCategory,
                        Collectors.groupingBy(
                                CourseVariantDetailDto::getCollection,
                                Collectors.mapping(
                                        CourseVariantDetailDto::getSubject,
                                        Collectors.collectingAndThen(
                                                Collectors.toSet(),
                                                ArrayList::new
                                        )
                                )
                        )
                ));

        return grouped.entrySet().stream()
                .map(entry -> {
                    String category = entry.getKey();
                    Map<String, List<String>> collectionsMap = entry.getValue();

                    List<CollectionVariantDto> collections = collectionsMap.entrySet().stream()
                            .map(colEntry ->
                                    new CollectionVariantDto(
                                            colEntry.getKey(),
                                            colEntry.getValue()
                                    )
                            )
                            .collect(Collectors.toList());

                    return new CategoryVariantDto(category, collections);
                })
                .collect(Collectors.toList());
    }

    /** Utility method */
    private boolean isValid(String value) {
        return value != null && !value.trim().isEmpty();
    }


    @Override
    public Map<String, Object> toggleIsActive(UUID courseId) {
        Map<String, Object> response = new HashMap<>();
        Course course = repository.findById(courseId).orElse(null);
        if (course == null) {
            response.put("message", "Course not found");
            response.put("error", true);
            return response;
        }
        // Toggle the isActive status
        course.setIsActive(!course.getIsActive());
        repository.save(course);
        response.put("message", "Course active status toggled successfully");
        response.put("error", false);
        return response;
    }

    @Override
    public Map<String, Object> toggleAttemptIsActive(UUID courseAttemptId) {
        Map<String, Object> response = new HashMap<>();
        CourseAttempt courseAttempt = courseAttemptRepository.findById(courseAttemptId).orElse(null);
        if (courseAttempt == null) {
            response.put("message", "Course Attempt not found");
            response.put("error", true);
            return response;
        }
        courseAttempt.setIsActive(!courseAttempt.getIsActive());
        courseAttemptRepository.save(courseAttempt);
        response.put("message", "Course Attempt status updated successfully");
        response.put("error", false);
        return response;
    }

    @Override
    public Map<String, Object> toggleBatchIsActive(UUID courseBatchId) {
        Map<String, Object> response = new HashMap<>();
        CourseBatch courseBatch = courseBatchRepository.findById(courseBatchId).orElse(null);
        if (courseBatch == null) {
            response.put("message", "Course Batch not found");
            response.put("error", true);
            return response;
        }
        courseBatch.setIsActive(!courseBatch.getIsActive());
        courseBatchRepository.save(courseBatch);
        response.put("message", "Course Batch status updated successfully");
        response.put("error", false);
        return response;
    }

    @Override
    public Map<String, Object> updateProductStatus(UUID productId, CourseStatusUpdateDto courseStatusUpdateDto) {
        Map<String, Object> response = new HashMap<>();
        Course existingCourse = repository.findById(productId).orElse(null);
        if (existingCourse == null) {
            response.put("message", "Course not found");
            response.put("error", true);
            return response;
        }

        if (courseStatusUpdateDto.getOnBanner() != null) {
            existingCourse.setOnBanner(courseStatusUpdateDto.getOnBanner());
        }
        if (courseStatusUpdateDto.getIsActive() != null) {
            existingCourse.setIsActive(courseStatusUpdateDto.getIsActive());
        }
        if (courseStatusUpdateDto.getIsPopular() != null) {
            existingCourse.setIsPopular(courseStatusUpdateDto.getIsPopular());
        }

        repository.save(existingCourse);
        response.put("message", "Course updated successfully.");
        response.put("error", false);
        return response;
    }

    @Override
    public Map<String, Object> deleteCourse(UUID courseId) {
        Map<String, Object> response = new HashMap<>();

        // Fetch the course by ID
        Course course = repository.findById(courseId).orElse(null);
        if (course == null) {
            response.put("message", "Course not found.");
            response.put("error", true);
            return response;
        }

        try {
            // Delete the course, which will also delete associated course types due to cascading delete
            repository.delete(course);
        } catch (Exception e) {
            response.put("message", "Failed to delete course.");
            response.put("error", true);
            return response;
        }

        response.put("message", "Course deleted successfully.");
        response.put("error", false);
        return response;
    }

    @Override
    public void deleteCourseAttempt(UUID id) {
        CourseAttempt courseAttempt = courseAttemptRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CourseAttempt not found with ID: " + id));

        // Check if the Course-Attempt is linked in Enrollment
        if (enrollmentRepository.existsByCourseAttempt(courseAttempt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CourseAttempt cannot be deleted as it is linked to an enrollment.");
        }

        // Check if the Course-Attempt is linked in Bundle
        if (courseBundleRepository.existsByCourseAttempts(courseAttempt)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CourseAttempt cannot be deleted as it is linked to an Bundle.");
        }

        // Check if the Course-Attempt exists in CartItem
        List<CartItem> cartItems = cartItemRepository.findByCourseAttempt(courseAttempt);
        if (!cartItems.isEmpty()) {
            cartItemRepository.deleteAll(cartItems);
        }

        // Delete the Course-Attempt
        courseAttemptRepository.delete(courseAttempt);
    }

}