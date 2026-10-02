package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.courseBundle.CourseBundleAddDto;
import com.affycloud.TutorioBackend.dtos.courseBundle.CourseBundleDetailDto;
import com.affycloud.TutorioBackend.dtos.courseBundle.CourseBundleSummaryDetailDto;
import com.affycloud.TutorioBackend.dtos.courseBundle.CourseBundleUpdateDto;
import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.handler.InvalidOperationException;
import com.affycloud.TutorioBackend.handler.ResourceNotFoundException;
import com.affycloud.TutorioBackend.models.*;
import com.affycloud.TutorioBackend.repositories.*;
import com.affycloud.TutorioBackend.services.CourseBundleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CourseBundleServiceImplementation implements CourseBundleService {

    @Autowired
    private CourseAttemptRepository courseAttemptRepository;

    @Autowired
    private CourseBundleRepository bundleRepository;

    @Autowired
    private R2ImageServiceImplementation r2ImageService;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public Map<String, Object> createCourseBundle(CourseBundleAddDto request, MultipartFile thumbnail,User user) throws IOException {
        Map<String, Object> response = new HashMap<>();

        // Fetch course types
        List<CourseAttempt> courseAttempts = courseAttemptRepository.findAllById(request.getCourseAttemptsIds());
        if (courseAttempts.isEmpty()) {
            throw new IllegalArgumentException("Invalid course type IDs.");
        }



        // Save course bundle
        CourseBundle courseBundle = new CourseBundle();
        courseBundle.setTitle(request.getTitle());
        courseBundle.setDescription(request.getDescription());

        if(user.getRole() == Role.ADMIN){
            courseBundle.setCreatedById(user.getId());
            courseBundle.setTenantId(user.getId());

        }
        else if (user.getRole() == Role.TEACHER) {
            courseBundle.setCreatedById(user.getId());
            courseBundle.setTenantId(user.getParent().getId());
        }

        // For PAID courses, require all fields
        if (
                request.getPrice() == null ||
                request.getDiscountType() == null ||
                request.getDiscount() == null) {
            response.put("message", "Price, discountType, and discount are required for PAID courses.");
            response.put("error", true);
            return response;
        }

        Double price = request.getPrice();
        String discountType = request.getDiscountType().toLowerCase();
        Double discount = request.getDiscount();

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

        courseBundle.setPrice(price);
        courseBundle.setDiscountType(discountType);
        courseBundle.setDiscount(discount);
        courseBundle.setFinalPrice(calculateFinalPrice(price, discount, discountType));
        courseBundle.setCourseAttempts(courseAttempts);

        // Handle thumbnail upload using FileStorageUtil
        if (thumbnail != null && !thumbnail.isEmpty()) {
            // upload new one
            String imageUrl = r2ImageService.uploadImage(thumbnail);
            courseBundle.setThumbnail(imageUrl);
        }
        bundleRepository.save(courseBundle);

        response.put("message", "Course created successfully.");
        response.put("error", false);
        return response;
    }

    @Override
    public Map<String, Object> updateCourseBundle(UUID id, CourseBundleUpdateDto updateDto, MultipartFile thumbnail) throws IOException {
        Map<String, Object> response = new HashMap<>();

        // Fetch the existing bundle; throw exception if not found.
        CourseBundle bundle = bundleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course bundle not found"));

        // Update fields only if they are provided in the update DTO.
        if (updateDto.getTitle() != null) {
            bundle.setTitle(updateDto.getTitle());
        }
        if (updateDto.getDescription() != null) {
            bundle.setDescription(updateDto.getDescription());
        }
        if (updateDto.getPrice() != null) {
            bundle.setPrice(updateDto.getPrice());
        }
        if (updateDto.getDiscountType() != null) {
            bundle.setDiscountType(updateDto.getDiscountType().toLowerCase());
        }
        if (updateDto.getDiscount() != null) {
            bundle.setDiscount(updateDto.getDiscount());
        }

        // Handle thumbnail upload using R2ImageService
        if (thumbnail != null && !thumbnail.isEmpty()) {
            // delete old thumbnail
            r2ImageService.deleteFileByUrl(bundle.getThumbnail());
            // upload new one
            String imageUrl = r2ImageService.uploadImage(thumbnail);
            bundle.setThumbnail(imageUrl);
        }



        // Validation: Only perform these if any related field is updated.
        Double effectivePrice = bundle.getPrice();
        Double effectiveDiscount = bundle.getDiscount();
        String effectiveDiscountType = bundle.getDiscountType();

        if (effectivePrice != null && effectiveDiscount != null && effectiveDiscountType != null) {
            if ("flat".equals(effectiveDiscountType) && effectiveDiscount > effectivePrice - 1) {
                response.put("message", "Discount amount cannot be greater than price minus 1 when discount type is flat.");
                response.put("error", true);
                return response;
            }
            if ("percentage".equals(effectiveDiscountType) && effectiveDiscount > 98) {
                response.put("message", "Discount percentage cannot be more than 98%.");
                response.put("error", true);
                return response;
            }
            if (updateDto.getPrice() != null || updateDto.getDiscount() != null || updateDto.getDiscountType() != null) {
                bundle.setFinalPrice(calculateFinalPrice(bundle.getPrice(), bundle.getDiscount(), bundle.getDiscountType()));
            }
        }

        // Save the updated bundle.
        bundleRepository.save(bundle);
        response.put("message", "Course bundle updated successfully.");
        response.put("error", false);
        return response;
    }

    private Double calculateFinalPrice(Double price, Double discount, String discountType) {
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
    public Map<String, Object> addCourseAttemptToBundle(UUID bundleId, UUID courseAttemptId) {
        Map<String, Object> response = new HashMap<>();

        // Fetch the bundle; throw exception if not found.
        CourseBundle bundle = bundleRepository.findById(bundleId)
                .orElseThrow(() -> new ResourceNotFoundException("Course bundle not found"));

        // Fetch the course attempt; throw exception if not found.
        CourseAttempt courseAttempt = courseAttemptRepository.findById(courseAttemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Course attempt not found"));

        // Initialize list if null and check for duplicates.
        List<CourseAttempt> courseAttempts = bundle.getCourseAttempts();
        if (courseAttempts == null) {
            courseAttempts = new ArrayList<>();
        }
        if (courseAttempts.contains(courseAttempt)) {
            throw new InvalidOperationException("Course attempt already exists in the bundle");
        }

        // Add the new course attempt and save.
        courseAttempts.add(courseAttempt);
        bundle.setCourseAttempts(courseAttempts);
        bundleRepository.save(bundle);

        response.put("message", "Course attempt added to bundle successfully.");
        response.put("error", false);
        return response;
    }

    @Override
    public Map<String, Object> removeCourseAttemptFromBundle(UUID bundleId, UUID courseAttemptId) {
        Map<String, Object> response = new HashMap<>();

        // Fetch the bundle; throw exception if not found.
        CourseBundle bundle = bundleRepository.findById(bundleId).
                orElseThrow(() -> new ResourceNotFoundException("Course bundle not found"));

        // Ensure there are course attempts in the bundle.
        List<CourseAttempt> courseAttempts = bundle.getCourseAttempts();
        if (courseAttempts == null || courseAttempts.isEmpty()) {
            throw new ResourceNotFoundException("No course attempts found in this bundle");
        }

        // Remove the course attempt based on the given id.
        boolean removed = courseAttempts.removeIf(courseAttempt -> courseAttempt.getId().equals(courseAttemptId));
        if (!removed) {
            throw new ResourceNotFoundException("Course attempt not found in the bundle");
        }

        bundle.setCourseAttempts(courseAttempts);
        bundleRepository.save(bundle);

        response.put("message", "Course attempt removed from bundle successfully.");
        response.put("error", false);
        return response;
    }

    @Override
    public CourseBundleDetailDto getBundleById(UUID id,UUID tenantId) {
        CourseBundle courseBundle = bundleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course bundle not found"));
        if(courseBundle.getTenantId() != tenantId){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bundle not found.");
        }
        // ✅ Use your manual mapping logic
        return CourseBundleDetailDto.fromEntity(courseBundle); // ✅ manual mapping method
    }

    @Override
    public CourseBundleDetailDto getBundleById(UUID id, UUID tenantId, UUID studentId) {
        CourseBundle courseBundle = bundleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course bundle not found"));

        if(courseBundle.getTenantId() != tenantId){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bundle not found.");
        }

        Set<UUID> courseBundleIdsInCart = cartItemRepository.findCourseBundleIdsByStudentId(studentId);

        // ✅ Use your manual mapping logic
        CourseBundleDetailDto dto = CourseBundleDetailDto.fromEntity(courseBundle);
        dto.setInCart(courseBundleIdsInCart.contains(id));

        return dto;
    }


    @Override
    public List<CourseBundleSummaryDetailDto> getAllBundles(UUID id) {
        List<CourseBundle> courseBundles = bundleRepository.findAllByTenantId(id, Sort.by("createdAt").descending());
        if (courseBundles.isEmpty()) {
            /*throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No bundles found");*/
            return Collections.emptyList();
        }
        return courseBundles.stream()
                .map(bundle -> modelMapper.map(bundle, CourseBundleSummaryDetailDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseBundle> getBundlesByFilters(String category, String collection, String subject) {
        List<CourseBundle> bundles = bundleRepository.findAll();
        return bundles.stream().filter(bundle -> {
            if (bundle.getCourseAttempts() == null || bundle.getCourseAttempts().isEmpty()) {
                return false;
            }
            // Check if any course in the bundle matches the filters
            return bundle.getCourseAttempts().stream().anyMatch(attempt -> {
                // Navigate from CourseAttempt -> CourseType -> Course
                Course course = attempt.getCourseType().getCourse();
                boolean catMatch = (category == null) || (course.getCategory() != null && course.getCategory().equalsIgnoreCase(category));
                boolean collMatch = (collection == null) || (course.getCollection() != null && course.getCollection().equalsIgnoreCase(collection));
                boolean subjMatch = (subject == null) || (course.getSubject() != null && course.getSubject().equalsIgnoreCase(subject));
                return catMatch && collMatch && subjMatch;
            });
        }).collect(Collectors.toList());
    }

    @Override
    public List<CourseBundle> getBundlesByFiltersByTenant(String category, String collection, String subject, UUID tenantId) {
        List<CourseBundle> bundles = bundleRepository.findAllByTenantId(tenantId, Sort.by("createdAt").descending());
        return bundles.stream().filter(bundle -> {
            if (bundle.getCourseAttempts() == null || bundle.getCourseAttempts().isEmpty()) {
                return false;
            }
            // Check if any course in the bundle matches the filters
            return bundle.getCourseAttempts().stream().anyMatch(attempt -> {
                // Navigate from CourseAttempt -> CourseType -> Course
                Course course = attempt.getCourseType().getCourse();
                boolean catMatch = (category == null) || (course.getCategory() != null && course.getCategory().equalsIgnoreCase(category));
                boolean collMatch = (collection == null) || (course.getCollection() != null && course.getCollection().equalsIgnoreCase(collection));
                boolean subjMatch = (subject == null) || (course.getSubject() != null && course.getSubject().equalsIgnoreCase(subject));
                return catMatch && collMatch && subjMatch;
            });
        }).collect(Collectors.toList());
    }


    @Override
    public void deleteCourseBundle(UUID id) {
        CourseBundle bundle = bundleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "bundle not found with ID: " + id));


        // Check if the Ebook exists in CartItem
    List<CartItem> cartItems = cartItemRepository.findByCourseBundle(bundle);
        if (!cartItems.isEmpty()) {
            cartItemRepository.deleteAll(cartItems);
        }

        // Delete the Ebook
        bundleRepository.delete(bundle);
    }
}
