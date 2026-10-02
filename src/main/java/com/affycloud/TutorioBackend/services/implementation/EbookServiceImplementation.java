package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.ebook.EbookAddDto;
import com.affycloud.TutorioBackend.dtos.ebook.EbookDetailDto;
import com.affycloud.TutorioBackend.dtos.ebook.EbookUpdateDto;
import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.models.*;
import com.affycloud.TutorioBackend.repositories.CartItemRepository;
import com.affycloud.TutorioBackend.repositories.EbookRepository;
import com.affycloud.TutorioBackend.repositories.EnrollmentRepository;
import com.affycloud.TutorioBackend.services.EbookService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Transactional
public class EbookServiceImplementation implements EbookService{

     @Autowired
    private EbookRepository ebookRepository;

    @Autowired
    private R2ImageServiceImplementation r2FileStorageService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Page<Map<String, Object>> getAllEbooksByStudent(Pageable pageable, UUID studentId, String paymentType, UUID tenantId) {
        // Retrieve all ebooks
        Page<Ebook> ebooksPage;
        if (paymentType != null && !paymentType.trim().isEmpty()) {
            PaymentType pt;
            try {
                pt = PaymentType.valueOf(paymentType.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid paymentType provided: " + paymentType);
            }
            ebooksPage = ebookRepository
                    .findAllByTenantIdAndPaymentTypeAndCourseTypeIdIsNull(tenantId, pt, pageable);
        } else {
            ebooksPage = ebookRepository.findAllByTenantIdAndCourseTypeIdIsNull(pageable, tenantId);
        }


        // Fetch testSeries IDs in cart and enrolled
        Set<UUID> ebookIdsInCart = cartItemRepository.findEbookIdsByStudentId(studentId);
        Set<UUID> enrolledEbookIds = enrollmentRepository.findEbookIdsByStudentId(studentId);


    // Convert Page<Ebook> to Page<Map<String, Object>>
        return ebooksPage.map(ebook -> {
            Map<String, Object> ebookMap = new HashMap<>();
            ebookMap.put("id", ebook.getId());
            ebookMap.put("title", ebook.getTitle());
            ebookMap.put("description", ebook.getDescription());
            ebookMap.put("price", ebook.getPrice());
            ebookMap.put("discountType", ebook.getDiscountType());
            ebookMap.put("discount", ebook.getDiscount());
            ebookMap.put("finalPrice", ebook.getFinalPrice());
            ebookMap.put("itemType", ebook.getItemType());
            ebookMap.put("paymentType", ebook.getPaymentType());
            ebookMap.put("courseTypeId", ebook.getCourseTypeId());
            ebookMap.put("url", ebook.getUrl());
            ebookMap.put("thumbnailUrl", ebook.getThumbnailUrl());
            ebookMap.put("createdAt", ebook.getCreatedAt());
            // Mark as inCart if the ebook's ID is found in the student's cart
            ebookMap.put("inCart", ebookIdsInCart.contains(ebook.getId()));
            ebookMap.put("inMyLearning", enrolledEbookIds.contains(ebook.getId()));
            return ebookMap;
        });
    }


     @Override
    public Page<Ebook> getAllEbooks(Pageable pageable,String paymentType, UUID tenantId) {
         Page<Ebook> ebooksPage;
         if (paymentType != null && !paymentType.trim().isEmpty()) {
             PaymentType pt;
             try {
                 pt = PaymentType.valueOf(paymentType.toUpperCase());
             } catch (IllegalArgumentException e) {
                 throw new RuntimeException("Invalid paymentType provided: " + paymentType);
             }
             ebooksPage = ebookRepository
                     .findAllByTenantIdAndPaymentTypeAndCourseTypeIdIsNull(tenantId, pt, pageable);
         } else {
             ebooksPage = ebookRepository.findAllByTenantIdAndCourseTypeIdIsNull(pageable, tenantId);
         }
        return ebooksPage;
    }

    public Map<String, Object> getEbooksByCourseType(UUID courseTypeId) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> meta = new ArrayList<>();

        try {
            List<Ebook> videos = ebookRepository.findByCourseTypeId(courseTypeId, Sort.by(Sort.Direction.ASC, "createdAt"));

            if (videos.isEmpty()) {
                response.put("meta", meta);  // Empty list when no videos found
                response.put("message", "Ebooks retrieved successfully");
                response.put("error", false);
            } else {
                response.put("meta", videos);
                response.put("message", "Ebooks fetched successfully");
                response.put("error", false);
            }
        } catch (Exception e) {
            response.put("meta", meta);  // Empty list in case of error
            response.put("message", "Error fetching videos");
            response.put("error", true);
            response.put("errorDetails", e.getMessage());
        }

        return response;
    }

    @Override
    public EbookDetailDto getEbookById(UUID id, UUID tenantId) {
        Ebook ebook = ebookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ebook not found with ID: " + id));

        if (!Objects.equals(ebook.getTenantId(), tenantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ebook not found.");
        }

        return modelMapper.map(ebook, EbookDetailDto.class);

    }


    @Override
    public EbookDetailDto getEbookById(UUID id, UUID tenantId, UUID studentId) {
        Ebook ebook = ebookRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ebook not found with ID: " + id));

        if (!Objects.equals(ebook.getTenantId(), tenantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ebook not found.");
        }

        Set<UUID> ebookIdsInCart = cartItemRepository.findEbookIdsByStudentId(studentId);

        // Check if the ebook is in Enrollment
        Set<UUID> enrolledEbookIds = enrollmentRepository.findEbookIdsByStudentId(studentId);

        EbookDetailDto dto = modelMapper.map(ebook, EbookDetailDto.class);

        dto.setInCart(ebookIdsInCart.contains(id));
        dto.setInMyLearning(enrolledEbookIds.contains(id));

        return dto;
    }


    @Override
    public Ebook uploadEbook(EbookAddDto request, User user) {

        PaymentType paymentType = validatePayment(request);

        Ebook ebook = new Ebook();
        ebook.setTitle(request.getTitle());
        ebook.setDescription(request.getDescription());
        ebook.setPaymentType(paymentType);
        ebook.setUrl(request.getEbookUrl());
        ebook.setThumbnailUrl(request.getThumbnailUrl());

        setTenantAndCreator(user, ebook);
        applyPricing(request, ebook);

        if (request.getCourseTypeId() != null) {
            ebook.setCourseTypeId(request.getCourseTypeId());
        }

        return ebookRepository.save(ebook);
    }

    @Override
    @Transactional
    public Map<String, Object> updateEbook(UUID id, EbookUpdateDto request) {

        Map<String, Object> response = new HashMap<>();

        Ebook ebook = ebookRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Ebook not found with ID: " + id
                        ));

        // -------- BASIC FIELDS --------
        if (request.getTitle() != null) ebook.setTitle(request.getTitle());
        if (request.getDescription() != null) ebook.setDescription(request.getDescription());

        // -------- PRICING --------
        applyPricingUpdate(request, ebook);

        // -------- FILE REPLACEMENT (presigned URLs from frontend) --------
        if (request.getEbookUrl() != null && !request.getEbookUrl().isBlank()) {
            // delete old ebook file from R2
            r2FileStorageService.deleteFileByUrl(ebook.getUrl());
            ebook.setUrl(request.getEbookUrl());
        }

        if (request.getThumbnailUrl() != null && !request.getThumbnailUrl().isBlank()) {
            // delete old thumbnail from R2
            r2FileStorageService.deleteFileByUrl(ebook.getThumbnailUrl());
            ebook.setThumbnailUrl(request.getThumbnailUrl());
        }

        ebookRepository.save(ebook);

        response.put("message", "Ebook updated successfully.");
        response.put("error", false);
        response.put("meta", ebook);
        return response;
    }

    @Override
    @Transactional
    public void deleteEbook(UUID id) {

        Ebook ebook = ebookRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Ebook not found with ID: " + id
                        ));

        if (enrollmentRepository.existsByEbook(ebook)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ebook cannot be deleted as it is linked to an enrollment."
            );
        }

        // Remove cart items
        List<CartItem> cartItems = cartItemRepository.findByEbook(ebook);
        if (!cartItems.isEmpty()) {
            cartItemRepository.deleteAll(cartItems);
        }

        // 🔥 DELETE FROM R2 FIRST
        r2FileStorageService.deleteFileByUrl(ebook.getUrl());
        r2FileStorageService.deleteFileByUrl(ebook.getThumbnailUrl());

        // Delete DB record
        ebookRepository.delete(ebook);
    }




   /* @Override
    public Map<String, Object> updateEbook(UUID id, MultipartFile file, MultipartFile thumbnail, EbookUpdateDto request) {

        Map<String, Object> response = new HashMap<>();

        Optional<Ebook> optionalEbook = ebookRepository.findById(id);
        if (optionalEbook.isPresent()) {
            Ebook ebook = optionalEbook.get();

            if (request.getTitle() != null) ebook.setTitle(request.getTitle());
            if (request.getDescription() != null) ebook.setDescription(request.getDescription());

            if (ebook.getPaymentType() == PaymentType.PAID) {
                if (request.getPrice() == null ||
                        request.getDiscountType() == null ||
                        request.getDiscount() == null) {
                    response.put("message", "Price, discountType, and discount are required for PAID ebooks");
                    response.put("error", true);
                    return response;
                }
                Double price = request.getPrice();
                String discountType = request.getDiscountType().toLowerCase();
                Double discount = request.getDiscount();
                // **Validation: Discount should not exceed price if type is "flat"**
                if ("flat".equals(discountType) && discount > price - 1) {
                    response.put("message", "Discount amount cannot be greater or equal than price when discount type is flat.");
                    response.put("error", true);
                    return response;
                }

                // **Validation: Discount should not exceed 98% if type is "percentage"**
                if ("percentage".equals(discountType) && discount > 98) {
                    response.put("message", "Discount percentage cannot be more than 98%.");
                    response.put("error", true);
                    return response;
                }

                ebook.setPrice(price);
                ebook.setDiscountType(discountType);
                ebook.setDiscount(discount);
                ebook.setFinalPrice(calculateFinalPrice(price, discount, discountType));

            } else { // FREE course
                ebook.setPrice(0.0);
                ebook.setDiscountType("none");
                ebook.setDiscount(0.0);
                ebook.setFinalPrice(0.0);
            }

            *//*if (file != null) {
                try {
                    String fileUrl = googleDriveEbookService.uploadEbook(file);
                    ebook.setUrl(fileUrl);
                } catch (Exception e) {
                    throw new RuntimeException("Error updating ebook file: " + e.getMessage());
                }
            }*//*

            if (thumbnail != null) {
                try {
                    String thumbnailUrl = FileStorageUtil.saveThumbnail(thumbnail);
                    ebook.setThumbnailUrl(thumbnailUrl);
                } catch (Exception e) {
                    throw new RuntimeException("Error updating ebook thumbnail: " + e.getMessage());
                }
            }

            ebookRepository.save(ebook);
            response.put("message", "ebook updated successfully.");
            response.put("error", false);
            return response;
        } else {
            response.put("message", "Failed to update ebook");
            response.put("error", true);
            return response;
        }
    }


    @Override
    public void deleteEbook(UUID id) {
        Ebook ebook = ebookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ebook not found with ID: " + id));

        // Check if the Ebook is linked in Enrollment
        if (enrollmentRepository.existsByEbook(ebook)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ebook cannot be deleted as it is linked to an enrollment.");
        }

        // Check if the Ebook exists in CartItem
        List<CartItem> cartItems = cartItemRepository.findByEbook(ebook);
        if (!cartItems.isEmpty()) {
            cartItemRepository.deleteAll(cartItems);
        }

        // Delete the Ebook
        ebookRepository.delete(ebook);
    }*/

    // ---------------- PRIVATE METHODS ----------------

    private PaymentType validatePayment(EbookAddDto request) {
        if (request.getPaymentType() == null) {
            request.setPaymentType(PaymentType.FREE);
        }

        if (request.getPaymentType() != PaymentType.FREE &&
                request.getPaymentType() != PaymentType.PAID) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid paymentType"
            );
        }
        return request.getPaymentType();
    }

    private void setTenantAndCreator(User user, Ebook ebook) {
        if (user.getRole() == Role.ADMIN) {
            ebook.setCreatedById(user.getId());
            ebook.setTenantId(user.getId());
        } else {
            ebook.setCreatedById(user.getId());
            ebook.setTenantId(user.getParent().getId());
        }
    }

    private void applyPricing(EbookAddDto request, Ebook ebook) {

        if (ebook.getPaymentType() == PaymentType.PAID) {

            Double price = request.getPrice();
            Double discount = request.getDiscount();
            String discountType = request.getDiscountType().toLowerCase();

            if ("flat".equals(discountType) && discount >= price) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid flat discount"
                );
            }

            if ("percentage".equals(discountType) && discount > 98) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid percentage discount"
                );
            }

            ebook.setPrice(price);
            ebook.setDiscount(discount);
            ebook.setDiscountType(discountType);
            ebook.setFinalPrice(
                    calculateFinalPrice(price, discount, discountType)
            );

        } else {
            ebook.setPrice(0.0);
            ebook.setFinalPrice(0.0);
            ebook.setDiscount(0.0);
            ebook.setDiscountType("none");
        }
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

    private void applyPricingUpdate(EbookUpdateDto request, Ebook ebook) {

        if (ebook.getPaymentType() == PaymentType.PAID) {

            Double price = request.getPrice();
            String discountType = request.getDiscountType();

            if (price == null || discountType == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Price, discountType and discount are required for PAID ebooks"
                );
            }

            discountType = discountType.toLowerCase();

            // "none" discount type means no discount — treat null discount as 0
            Double discount = "none".equals(discountType) && request.getDiscount() == null
                    ? 0.0
                    : request.getDiscount();

            if (discount == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Price, discountType and discount are required for PAID ebooks"
                );
            }

            if ("flat".equals(discountType) && discount >= price) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid flat discount"
                );
            }

            if ("percentage".equals(discountType) && discount > 98) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid percentage discount"
                );
            }

            ebook.setPrice(price);
            ebook.setDiscount(discount);
            ebook.setDiscountType(discountType);
            ebook.setFinalPrice(calculateFinalPrice(price, discount, discountType));

        } else {
            ebook.setPrice(0.0);
            ebook.setDiscount(0.0);
            ebook.setDiscountType("none");
            ebook.setFinalPrice(0.0);
        }
    }


}
