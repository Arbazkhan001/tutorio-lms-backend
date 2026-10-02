package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.coupon.CouponDetailDTO;
import com.affycloud.TutorioBackend.dtos.enrollment.EnrollmentDTO;
import com.affycloud.TutorioBackend.dtos.order.OrderDetailsDto;
import com.affycloud.TutorioBackend.dtos.order.OrderRequestDto;
import com.affycloud.TutorioBackend.dtos.order.orderItems.OrderItemResponseDto;
import com.affycloud.TutorioBackend.enums.ItemType;
import com.affycloud.TutorioBackend.handler.InvalidOperationException;
import com.affycloud.TutorioBackend.handler.ResourceNotFoundException;
import com.affycloud.TutorioBackend.models.*;
import com.affycloud.TutorioBackend.repositories.*;
import com.affycloud.TutorioBackend.services.OrderService;
import com.affycloud.TutorioBackend.utils.PhoneNumberUtil;
import com.razorpay.RazorpayException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class OrderServiceImplementation implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImplementation.class);

    @Autowired
    private ModelMapper modelMapper;

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CouponRepository couponRepository;
    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RazorpayServiceImplementation razorpayService;

    public OrderServiceImplementation(RazorpayServiceImplementation razorpayService, OrderRepository orderRepository,
            CartRepository cartRepository,
            AdminRepository adminRepository, CouponRepository couponRepository, StudentRepository studentRepository,
            EnrollmentRepository enrollmentRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.adminRepository = adminRepository;
        this.couponRepository = couponRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.razorpayService = razorpayService;
    }

    /**
     * Validates the profile details Razorpay needs before an order is placed
     * and, when possible, permanently normalizes the phone number.
     *
     * <p>The normalized number is only persisted when no OTHER student in the
     * same tenant already owns it — the {@code uq_student_tenant_phone} unique
     * constraint would otherwise fail the whole order transaction. When a
     * conflict exists the stored value is left untouched (never mutate the
     * managed entity here: dirty checking would flush the change at commit
     * even without an explicit save) and the order proceeds normally.
     *
     * @return {@code true} if the profile is usable and the order flow may
     *         continue; {@code false} if {@code response} was populated with
     *         an error for the caller to return.
     */
    private boolean validateAndSanitizeStudentPhone(Student student, Map<String, Object> response) {
        /* Razorpay's payment-link customer block needs a name as well as a
         * contact. A null name used to sail past this gate and come back as an
         * opaque "Failed to generate payment link" from the gateway, which the
         * checkout page could not turn into anything the student could act on. */
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            response.put("message", "Name is required. Please update your profile.");
            response.put("error", true);
            return false;
        }

        String phone = student.getPhoneNumber();
        String sanitizedPhone = PhoneNumberUtil.normalize(phone);
        if (sanitizedPhone == null) {
            /* Covers null, "", whitespace, and values holding no digits at all. */
            response.put("message", "Phone number is required. Please update your profile.");
            response.put("error", true);
            return false;
        }

        // Exceptional case: some international numbers have fewer than 8 digits,
        // which Razorpay rejects. Extend ONLY those by prefixing the country-code
        // digits (or appending random digits when no country code is stored).
        // Numbers already 8-14 characters long are never modified.
        if (sanitizedPhone.length() < 8) {
            sanitizedPhone = buildRazorpayCompatiblePhone(student, sanitizedPhone);
            if (sanitizedPhone == null) {
                response.put("message", "Phone number must be between 8 and 14 digits. Please update your profile.");
                response.put("error", true);
                return false;
            }
        }

        if (sanitizedPhone.length() > 14) {
            response.put("message", "Phone number must be between 8 and 14 digits. Please update your profile.");
            response.put("error", true);
            return false;
        }

        if (!sanitizedPhone.equals(phone)) {
            Student conflict = studentRepository.findByTenantIdAndPhoneNumber(
                    student.getTenantId(), sanitizedPhone);
            if (conflict == null || conflict.getId().equals(student.getId())) {
                student.setPhoneNumber(sanitizedPhone);
                studentRepository.save(student);
            } else {
                logger.warn("Skipped phone normalization for student {}: sanitized number already "
                                + "belongs to student {} in tenant {}. Order will proceed with the stored value.",
                        student.getId(), conflict.getId(), student.getTenantId());
            }
        }
        return true;
    }

    /**
     * Builds a Razorpay-compatible (min 8 digit) phone number for students
     * whose real number is shorter than 8 digits — possible for some
     * international numbers.
     *
     * <p>Strategy: prefix the digits of the stored country code (e.g.
     * {@code +298} + {@code 1234567} → {@code 2981234567}). If that is still
     * too short, or no country code is stored, random digits are appended
     * until the 8-digit minimum is reached. The candidate is only returned
     * once it does not collide with another student's number in the same
     * tenant ({@code uq_student_tenant_phone}); collisions are retried with
     * fresh random digits.
     *
     * @return a unique 8-14 character phone number, or {@code null} if no
     *         unique candidate could be found (caller returns an error).
     */
    private String buildRazorpayCompatiblePhone(Student student, String shortPhone) {
        String phoneDigits = shortPhone.replaceAll("\\D", "");
        String countryCodeDigits = student.getCountryCode() == null
                ? ""
                : student.getCountryCode().replaceAll("\\D", "");
        String base = countryCodeDigits + phoneDigits;

        for (int attempt = 0; attempt < 10; attempt++) {
            StringBuilder candidate = new StringBuilder(base);
            // After a collision, grow the target by one digit per attempt so new
            // candidates open up even when the base itself is already 8+ digits.
            int targetLength = Math.min(14, Math.max(8, base.length() + attempt));
            while (candidate.length() < targetLength) {
                candidate.append(ThreadLocalRandom.current().nextInt(10));
            }
            String result = candidate.toString();

            Student conflict = studentRepository.findByTenantIdAndPhoneNumber(
                    student.getTenantId(), result);
            if (conflict == null || conflict.getId().equals(student.getId())) {
                logger.info("Extended short phone number '{}' to '{}' for student {} (country code: {})",
                        shortPhone, result, student.getId(), student.getCountryCode());
                return result;
            }
        }

        logger.warn("Could not build a unique Razorpay-compatible phone number for student {} in tenant {}",
                student.getId(), student.getTenantId());
        return null;
    }

    @Override
    @Transactional
    public Map<String, Object> placeOrder(OrderRequestDto request) {
        Map<String, Object> response = new HashMap<>();

        Optional<Student> studentOptional = studentRepository.findById(request.getStudentId());
        if (studentOptional.isEmpty()) {
            response.put("message", "Student not found.");
            response.put("error", true);
            return response;
        }
        Student student = studentOptional.get();

        if (!validateAndSanitizeStudentPhone(student, response)) {
            return response;
        }

        Coupon coupon = null;

        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            Optional<Coupon> couponOptional = couponRepository.findByCode(request.getCouponCode());

            if (couponOptional.isEmpty()) {
                response.put("message", "Coupon not found.");
                response.put("error", true);
                return response;
            }

            coupon = couponOptional.get();

            if (!coupon.isActive()) {
                throw new InvalidOperationException("Coupon is not active");
            }

            if (coupon.getStudent() != null) {
                if (!coupon.getStudent().getId().equals(request.getStudentId())) {
                    throw new InvalidOperationException("This coupon is not assigned to this student");
                }
                if (coupon.isUsed()) {
                    throw new InvalidOperationException("Coupon has already been used");
                }
            }
        }

        Cart cart = cartRepository.findByStudentId(student.getId()).stream().findFirst().orElse(null);
        if (cart == null || cart.getCartItems().isEmpty()) {
            response.put("message", "Cart is empty.");
            response.put("error", true);
            return response;
        }

        Order order = new Order();
        order.setStudent(student);
        order.setStatus("PENDING"); // Payment pending
        order.setPaymentStatus("UNPAID"); // Payment UNPAID
        order.setCoupon(coupon);

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setQuantity(cartItem.getQuantity());
            double itemTotalPrice = 0.0;

            // Check each product type and store a snapshot of its details in OrderItem
            if (cartItem.getEbook() != null) {
                Ebook ebook = cartItem.getEbook();
                orderItem.setItemType(ItemType.EBOOK);
                orderItem.setItemId(ebook.getId());
                orderItem.setTitle(ebook.getTitle());
                orderItem.setPrice(ebook.getPrice());
                orderItem.setDiscountType(ebook.getDiscountType());
                orderItem.setDiscount(ebook.getDiscount());
                orderItem.setFinalPrice(ebook.getFinalPrice());
                itemTotalPrice = ebook.getFinalPrice() * cartItem.getQuantity();
            } else if (cartItem.getTestSeries() != null) {
                TestSeries testSeries = cartItem.getTestSeries();
                orderItem.setItemType(ItemType.TESTSERIES);
                orderItem.setItemId(testSeries.getId());
                orderItem.setTitle(testSeries.getTitle());
                orderItem.setPrice(testSeries.getPrice());
                orderItem.setDiscountType(testSeries.getDiscountType());
                orderItem.setDiscount(testSeries.getDiscount());
                orderItem.setFinalPrice(testSeries.getFinalPrice());
                itemTotalPrice = testSeries.getFinalPrice() * cartItem.getQuantity();
            } else if (cartItem.getBook() != null) {
                Book book = cartItem.getBook();
                orderItem.setItemType(ItemType.BOOK);
                orderItem.setItemId(book.getBookId());
                orderItem.setTitle(book.getTitle());
                orderItem.setPrice(book.getPrice());
                orderItem.setDiscountType(book.getDiscountType());
                orderItem.setDiscount(book.getDiscount());
                orderItem.setFinalPrice(book.getFinalPrice());
                itemTotalPrice = book.getFinalPrice() * cartItem.getQuantity();
            } else if (cartItem.getCourseAttempt() != null) {
                CourseAttempt courseAttempt = cartItem.getCourseAttempt();
                orderItem.setItemType(ItemType.COURSE);
                orderItem.setItemId(courseAttempt.getId());
                if (cartItem.getCourseBatch() != null) {
                    orderItem.setCourseBatchId(cartItem.getCourseBatch().getId());
                }
                orderItem.setCourseAttempt(courseAttempt.getAttempt());
                orderItem.setCourseType(courseAttempt.getCourseType().getCourseType());
                // Assuming 'attempt' field represents the course title/description
                orderItem.setTitle(courseAttempt.getCourseType().getCourse().getTitle());
                orderItem.setPrice(courseAttempt.getPrice());
                orderItem.setDiscountType(courseAttempt.getDiscountType());
                orderItem.setDiscount(courseAttempt.getDiscount());
                orderItem.setFinalPrice(courseAttempt.getFinalPrice());
                itemTotalPrice = courseAttempt.getFinalPrice() * cartItem.getQuantity();
            } else if (cartItem.getCourseBundle() != null) {
                CourseBundle courseBundle = cartItem.getCourseBundle();
                // Use a distinct type if available or consider it as COURSEBUNDLE
                orderItem.setItemType(ItemType.COURSEBUNDLE);
                orderItem.setItemId(courseBundle.getId());
                orderItem.setTitle(courseBundle.getTitle());
                orderItem.setPrice(courseBundle.getPrice());
                orderItem.setDiscountType(courseBundle.getDiscountType());
                orderItem.setDiscount(courseBundle.getDiscount());
                orderItem.setFinalPrice(courseBundle.getFinalPrice());
                itemTotalPrice = courseBundle.getFinalPrice() * cartItem.getQuantity();
            }
            orderItem.setTotalPrice(itemTotalPrice);
            totalAmount += itemTotalPrice;
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        order.setTenantId(student.getTenantId());
        order.setAmount(totalAmount);
        order.setDiscountAmount(coupon != null ? coupon.getDiscount() : 0.0);
        order.setTotalAmount(totalAmount - (coupon != null ? coupon.getDiscount() : 0.0));
        orderRepository.save(order);

        try {
            User admin = adminRepository.findById(student.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found for the student"));
            String paymentLink = razorpayService.generatePaymentLink(order, admin);
            /*
             * String signature = razorpayService.generateSignature(order.getId(),
             * paymentLink);
             */

            response.put("paymentLink", paymentLink);
            response.put("orderId", order.getId()); // Save orderId for webhook processing
            /* response.put("signature", signature); // Send signature for verification */
        } catch (RazorpayException e) {
            response.put("message", "Failed to generate payment link: " + e.getMessage());
            response.put("error", true);
            return response;
        }

        /*
         * if (coupon != null) {
         * coupon.setUsed(true);
         * couponRepository.save(coupon);
         * }
         */

        response.put("message", "Order placed successfully.");
        response.put("orderId", order.getId());
        response.put("error", false);
        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> createOrder(OrderRequestDto request) {
        Map<String, Object> response = new HashMap<>();

        Optional<Student> studentOptional = studentRepository.findById(request.getStudentId());
        if (studentOptional.isEmpty()) {
            response.put("message", "Student not found.");
            response.put("error", true);
            return response;
        }
        Student student = studentOptional.get();

        if (!validateAndSanitizeStudentPhone(student, response)) {
            return response;
        }

        Coupon coupon = null;

        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            Optional<Coupon> couponOptional = couponRepository.findByCode(request.getCouponCode());

            if (couponOptional.isEmpty()) {
                response.put("message", "Coupon not found.");
                response.put("error", true);
                return response;
            }

            coupon = couponOptional.get();

            if (!coupon.isActive()) {
                throw new InvalidOperationException("Coupon is not active");
            }

            if (coupon.getStudent() != null) {
                if (!coupon.getStudent().getId().equals(request.getStudentId())) {
                    throw new InvalidOperationException("This coupon is not assigned to this student");
                }
                if (coupon.isUsed()) {
                    throw new InvalidOperationException("Coupon has already been used");
                }
            }
        }

        Cart cart = cartRepository.findByStudentId(student.getId()).stream().findFirst().orElse(null);
        if (cart == null || cart.getCartItems().isEmpty()) {
            response.put("message", "Cart is empty.");
            response.put("error", true);
            return response;
        }

        Order order = new Order();
        order.setStudent(student);
        order.setStatus("PENDING"); // Payment pending
        order.setPaymentStatus("UNPAID"); // Payment UNPAID
        order.setCoupon(coupon); // Payment UNPAID

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setQuantity(cartItem.getQuantity());
            double itemTotalPrice = 0.0;

            // Check each product type and store a snapshot of its details in OrderItem
            if (cartItem.getEbook() != null) {
                Ebook ebook = cartItem.getEbook();
                orderItem.setItemType(ItemType.EBOOK);
                orderItem.setItemId(ebook.getId());
                orderItem.setTitle(ebook.getTitle());
                orderItem.setPrice(ebook.getPrice());
                orderItem.setDiscountType(ebook.getDiscountType());
                orderItem.setDiscount(ebook.getDiscount());
                orderItem.setFinalPrice(ebook.getFinalPrice());
                itemTotalPrice = ebook.getFinalPrice() * cartItem.getQuantity();
            } else if (cartItem.getTestSeries() != null) {
                TestSeries testSeries = cartItem.getTestSeries();
                orderItem.setItemType(ItemType.TESTSERIES);
                orderItem.setItemId(testSeries.getId());
                orderItem.setTitle(testSeries.getTitle());
                orderItem.setPrice(testSeries.getPrice());
                orderItem.setDiscountType(testSeries.getDiscountType());
                orderItem.setDiscount(testSeries.getDiscount());
                orderItem.setFinalPrice(testSeries.getFinalPrice());
                itemTotalPrice = testSeries.getFinalPrice() * cartItem.getQuantity();
            } else if (cartItem.getBook() != null) {
                Book book = cartItem.getBook();
                orderItem.setItemType(ItemType.BOOK);
                orderItem.setItemId(book.getBookId());
                orderItem.setTitle(book.getTitle());
                orderItem.setPrice(book.getPrice());
                orderItem.setDiscountType(book.getDiscountType());
                orderItem.setDiscount(book.getDiscount());
                orderItem.setFinalPrice(book.getFinalPrice());
                itemTotalPrice = book.getFinalPrice() * cartItem.getQuantity();
            } else if (cartItem.getCourseAttempt() != null) {
                CourseAttempt courseAttempt = cartItem.getCourseAttempt();
                orderItem.setItemType(ItemType.COURSE);
                orderItem.setItemId(courseAttempt.getId());
                if (cartItem.getCourseBatch() != null) {
                    orderItem.setCourseBatchId(cartItem.getCourseBatch().getId());
                }
                orderItem.setCourseAttempt(courseAttempt.getAttempt());
                orderItem.setCourseType(courseAttempt.getCourseType().getCourseType());
                // Assuming 'attempt' field represents the course title/description
                orderItem.setTitle(courseAttempt.getCourseType().getCourse().getTitle());
                orderItem.setPrice(courseAttempt.getPrice());
                orderItem.setDiscountType(courseAttempt.getDiscountType());
                orderItem.setDiscount(courseAttempt.getDiscount());
                orderItem.setFinalPrice(courseAttempt.getFinalPrice());
                itemTotalPrice = courseAttempt.getFinalPrice() * cartItem.getQuantity();
            } else if (cartItem.getCourseBundle() != null) {
                CourseBundle courseBundle = cartItem.getCourseBundle();
                // Use a distinct type if available or consider it as COURSEBUNDLE
                orderItem.setItemType(ItemType.COURSEBUNDLE);
                orderItem.setItemId(courseBundle.getId());
                orderItem.setTitle(courseBundle.getTitle());
                orderItem.setPrice(courseBundle.getPrice());
                orderItem.setDiscountType(courseBundle.getDiscountType());
                orderItem.setDiscount(courseBundle.getDiscount());
                orderItem.setFinalPrice(courseBundle.getFinalPrice());
                itemTotalPrice = courseBundle.getFinalPrice() * cartItem.getQuantity();
            }
            orderItem.setTotalPrice(itemTotalPrice);
            totalAmount += itemTotalPrice;
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        order.setAmount(totalAmount);
        order.setDiscountAmount(coupon != null ? coupon.getDiscount() : 0.0);
        order.setTotalAmount(totalAmount - (coupon != null ? coupon.getDiscount() : 0.0));
        orderRepository.save(order);

        try {

            User admin = adminRepository.findById(student.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found for the student"));

            Map<String, Object> razorpayOrder = razorpayService.createRazorpayOrder(order, admin);

            response.put("razorpayOrder", razorpayOrder);
            response.put("orderId", order.getId()); // Save orderId for webhook processing

        } catch (RazorpayException e) {
            response.put("message", "Failed to generate payment link: " + e.getMessage());
            response.put("error", true);
            return response;
        }

        /*
         * if (coupon != null) {
         * coupon.setUsed(true);
         * couponRepository.save(coupon);
         * }
         */

        response.put("message", "Order placed successfully.");
        response.put("orderId", order.getId());
        response.put("error", false);
        return response;
    }

    public OrderDetailsDto getOrderByOrderId(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Map the basic order properties (manual mapping is preferred to avoid lazy
        // collection issues)
        OrderDetailsDto dto = new OrderDetailsDto();
        dto.setOrderId(order.getId());
        dto.setStudentId(order.getStudent().getId());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setRazorPayId(order.getRazorPayId());
        dto.setRandomId(order.getRandomId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedAt(order.getCreatedAt());

        // Map coupon if present
        if (order.getCoupon() != null) {
            Coupon coupon = order.getCoupon();
            CouponDetailDTO couponDto = new CouponDetailDTO();
            couponDto.setCode(coupon.getCode());
            couponDto.setDescription(coupon.getDescription());
            couponDto.setDiscount(coupon.getDiscount());
            dto.setCoupon(couponDto);
        }

        // Manually map the orderItems collection
        List<OrderItemResponseDto> orderItemDtos = order.getOrderItems().stream().map(item -> {
            OrderItemResponseDto itemDto = new OrderItemResponseDto();
            itemDto.setOrderItemId(item.getId());
            itemDto.setItemType(item.getItemType());
            itemDto.setItemId(item.getItemId());
            itemDto.setCourseBatchId(item.getCourseBatchId());
            itemDto.setTitle(item.getTitle());
            itemDto.setCourseType(item.getCourseType());
            itemDto.setCourseAttempt(item.getCourseAttempt());
            itemDto.setPrice(item.getPrice());
            itemDto.setDiscountType(item.getDiscountType());
            itemDto.setDiscount(item.getDiscount());
            itemDto.setFinalPrice(item.getFinalPrice());
            itemDto.setTotalPrice(item.getTotalPrice());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setCreatedAt(item.getCreatedAt());
            return itemDto;
        }).collect(Collectors.toList());
        dto.setOrderItems(orderItemDtos);
        return dto;
    }

    public Page<OrderDetailsDto> getAllOrders(Pageable pageable) {
        /* Page<Order> ordersPage = orderRepository.findAll(pageable); */
        Page<Order> ordersPage = orderRepository.findByPaymentStatusNot("UNPAID", pageable);
        return ordersPage.map(this::convertToOrderDetailsDto);
    }

    public Page<OrderDetailsDto> getOrdersByStudentId(UUID studentId, Pageable pageable) {
        /*
         * Page<Order> ordersPage = orderRepository.findByStudentId(studentId,
         * pageable);
         */
        Page<Order> ordersPage = orderRepository.findByStudentIdAndPaymentStatusNot(studentId, "UNPAID", pageable);
        return ordersPage.map(this::convertToOrderDetailsDto);
    }

    public Page<OrderDetailsDto> getAllOrdersByStudentId(UUID studentId, Pageable pageable) {
        /*
         * Page<Order> ordersPage = orderRepository.findByStudentId(studentId,
         * pageable);
         */
        Page<Order> ordersPage = orderRepository.findByStudentIdAndPaymentStatusNot(studentId, "PAID", pageable);
        return ordersPage.map(this::convertToOrderDetailsDto);
    }

    private OrderDetailsDto convertToOrderDetailsDto(Order order) {
        OrderDetailsDto dto = new OrderDetailsDto();
        dto.setOrderId(order.getId());
        dto.setStudentId(order.getStudent().getId());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setRazorPayId(order.getRazorPayId());
        dto.setRandomId(order.getRandomId());
        dto.setAmount(order.getAmount());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedAt(order.getCreatedAt());
        // Map coupon if present
        if (order.getCoupon() != null) {
            Coupon coupon = order.getCoupon();
            CouponDetailDTO couponDto = new CouponDetailDTO();
            couponDto.setCode(coupon.getCode());
            couponDto.setDescription(coupon.getDescription());
            couponDto.setDiscount(coupon.getDiscount());
            dto.setCoupon(couponDto);
        }

        List<OrderItemResponseDto> orderItemDtos = order.getOrderItems().stream().map(item -> {
            OrderItemResponseDto itemDto = new OrderItemResponseDto();
            itemDto.setOrderItemId(item.getId());
            itemDto.setItemType(item.getItemType());
            itemDto.setItemId(item.getItemId());
            itemDto.setCourseBatchId(item.getCourseBatchId());
            itemDto.setTitle(item.getTitle());
            itemDto.setCourseType(item.getCourseType());
            itemDto.setCourseAttempt(item.getCourseAttempt());
            itemDto.setPrice(item.getPrice());
            itemDto.setDiscountType(item.getDiscountType());
            itemDto.setDiscount(item.getDiscount());
            itemDto.setFinalPrice(item.getFinalPrice());
            itemDto.setTotalPrice(item.getTotalPrice());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setCreatedAt(item.getCreatedAt());
            return itemDto;
        }).collect(Collectors.toList());

        dto.setOrderItems(orderItemDtos);
        return dto;
    }

    @Override
    public Page<EnrollmentDTO> getEnrolledItems(UUID studentId, Pageable pageable) {
        Page<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId, pageable);
        return enrollments.map(EnrollmentDTO::fromEntity);
    }

    @Override
    public Page<EnrollmentDTO> getEnrolledItemsByType(UUID studentId, ItemType itemType, Pageable pageable) {
        Page<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndItemType(studentId, itemType, pageable);
        return enrollments.map(EnrollmentDTO::fromEntity);
    }
}