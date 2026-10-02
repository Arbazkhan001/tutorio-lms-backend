package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.enrollment.EnrollmentDTO;
import com.affycloud.TutorioBackend.dtos.order.OrderDetailsDto;
import com.affycloud.TutorioBackend.dtos.order.OrderRequestDto;
import com.affycloud.TutorioBackend.dtos.order.OrderResponseDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.enums.ItemType;
import com.affycloud.TutorioBackend.enums.NotificationType;
import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.handler.ResourceNotFoundException;
import com.affycloud.TutorioBackend.models.*;
import com.affycloud.TutorioBackend.repositories.*;
import com.affycloud.TutorioBackend.services.EmailService;
import com.affycloud.TutorioBackend.services.InvoiceGeneratorService;
import com.affycloud.TutorioBackend.services.NotificationService;
import com.affycloud.TutorioBackend.services.OrderService;
import com.affycloud.TutorioBackend.services.RazorpayService;
import com.affycloud.TutorioBackend.utils.AuthUtilAdmin;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EbookRepository ebookRepository;

    @Autowired
    private AuthUtilAdmin authUtil;

    @Autowired
    private CourseAttemptRepository courseAttemptRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private TestSeriesRepository testSeriesRepository;

    @Autowired
    private CourseBundleRepository courseBundleRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CourseBatchRepository courseBatchRepository;

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private InvoiceGeneratorService invoiceGeneratorService;

    @Autowired
    private ModelMapper modelMapper;

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Operation(summary = "this api used by react front-end using payment link.")
    @PostMapping("/place")
    public ResponseEntity<Map<String, Object>> placeOrder(@RequestBody OrderRequestDto request,
                                                          HttpServletRequest httpRequest) {
        ResponseEntity<Map<String, Object>> denial = bindOrderToAuthenticatedStudent(request, httpRequest);
        if (denial != null) {
            return denial;
        }

        Map<String, Object> response = orderService.placeOrder(request);
        if (response.containsKey("error") && (boolean) response.get("error")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "this api used by react-native front-end without using payment link.")
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderRequestDto request,
                                                           HttpServletRequest httpRequest) {
        ResponseEntity<Map<String, Object>> denial = bindOrderToAuthenticatedStudent(request, httpRequest);
        if (denial != null) {
            return denial;
        }

        Map<String, Object> response = orderService.createOrder(request);
        if (response.containsKey("error") && (boolean) response.get("error")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    /**
     * Makes the access token, not the request body, decide whose order this is.
     *
     * <p>{@code studentId} arrives from the client, so a panel holding a stale
     * id would buy against - and enrol - a different student's account. When a
     * student token is present the id is overwritten with the one it resolves
     * to; a body that names somebody else is refused outright rather than
     * silently rewritten, so a genuinely confused client is visible instead of
     * quietly "working".
     *
     * @return {@code null} when the order may proceed.
     */
    private ResponseEntity<Map<String, Object>> bindOrderToAuthenticatedStudent(OrderRequestDto request,
                                                                                HttpServletRequest httpRequest) {
        Student authenticatedStudent = authUtil.getOptionalAuthenticatedStudent(httpRequest);
        if (authenticatedStudent == null) {
            /* No student session (react-native/admin-driven flows). Behaviour
             * is unchanged: the body id is used as before. */
            return null;
        }

        if (request.getStudentId() != null && !request.getStudentId().equals(authenticatedStudent.getId())) {
            logger.warn("Blocked order for student {} placed with the session of student {}",
                    request.getStudentId(), authenticatedStudent.getId());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "This order does not belong to the signed-in account. Please sign in again.");
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        request.setStudentId(authenticatedStudent.getId());
        return null;
    }

    @Operation(summary = "Check Payment Status")
    @GetMapping("/payment-status")
    public ResponseEntity<String> checkPaymentStatus(
            @RequestParam String orderId,
            @RequestParam UUID studentId,
            @RequestParam String razorpay_payment_id) {
        try {

            Optional<Student> optionalStudent = studentRepository.findById(studentId);
            if (optionalStudent.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid order ID.");
            }
            Student student = optionalStudent.get();
            String email = student.getEmail();
            User admin = adminRepository.findById(student.getTenantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found for the student"));
            boolean isPaid = razorpayService.verifyPayment(razorpay_payment_id, admin);
            Optional<Order> optionalOrder = orderRepository.findById(UUID.fromString(orderId));
            if (optionalOrder.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid order ID.");
            }
            Order order = optionalOrder.get();

            if (isPaid) {
                // Check if already processed (idempotency)
                if (!"PENDING".equals(order.getStatus())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order already processed.");
                }
                processSuccessfulPayment(order, student, razorpay_payment_id);
            } else {
                order.setStatus("FAILED");
                order.setPaymentStatus("UNPAID");
                orderRepository.save(order);
            }

            return ResponseEntity.ok(isPaid ? "Payment Verified Successfully" : "Payment Verification Failed");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @Operation(summary = "Send Order Confirmation Email (Test API)")
    @PostMapping("/send-order-email")
    public ResponseEntity<?> sendOrderConfirmationEmail(
            @RequestParam UUID orderId,
            @RequestParam String email) {

        try {
            // Fetch Order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

            // Optional: override student email for testing
            if (email != null && !email.isBlank()) {
                order.getStudent().setEmail(email);
            }

            // Generate premium email HTML via InvoiceGeneratorService
            String emailBody = generateEmailContent(order);

            // Send HTML email
            emailService.sendOrderConfirmationEmail(
                    email,
                    "Order Confirmation - " + order.getRandomId(),
                    emailBody);

            return ResponseEntity.ok("Order confirmation email sent successfully.");

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error sending email: " + e.getMessage());
        }
    }

    @Operation(summary = "Download Invoice as PNG")
    @GetMapping("/download-invoice-png")
    public ResponseEntity<?> downloadInvoicePng(@RequestParam UUID orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

            byte[] pngBytes = invoiceGeneratorService.generateInvoicePng(order);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData(
                    "attachment",
                    "invoice-" + order.getRandomId() + ".png"
            );

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pngBytes);

        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Order not found: " + ex.getMessage());
        } catch (Exception e) {
            logger.error("Failed to generate invoice PNG for order {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate invoice PNG: " + e.getMessage());
        }
    }

    /**
     * Generates a professional, branded invoice HTML email body.
     * Delegates to {@link InvoiceGeneratorService#generateEmailHtml(Order)}.
     *
     * <p>Kept as a public method so existing call-sites (payment-status and
     * webhook endpoints) continue to work without modification.
     */
    public String generateEmailContent(Order order) {
        return invoiceGeneratorService.generateEmailHtml(order);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Shared post-payment processing logic
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Processes a successfully paid order. This is the single source of truth
     * for all post-payment operations and is called by both
     * {@code checkPaymentStatus()} and {@code handleRazorpayWebhook()}.
     *
     * <p>Operations performed:
     * <ol>
     *   <li>Update order status (COMPLETED) and payment info</li>
     *   <li>Create enrollments (EBOOK, TESTSERIES, COURSE, COURSEBUNDLE)</li>
     *   <li>Clear the student's cart</li>
     *   <li>Send order confirmation email</li>
     *   <li>Mark coupon as used (if applicable)</li>
     * </ol>
     *
     * @param order              the order being processed (must be in PENDING status)
     * @param student            the student who placed the order
     * @param razorpayPaymentId  Razorpay payment ID (from payment entity)
     */
    private void processSuccessfulPayment(Order order, Student student, String razorpayPaymentId) {
        // 1. Update order status & payment info
        order.setStatus("COMPLETED");
        order.setPaymentStatus("PAID");
        order.setRazorPayPaymentId(razorpayPaymentId);

        // 2. Create enrollments
        List<Enrollment> enrollments = new ArrayList<>();
        for (OrderItem orderItem : order.getOrderItems()) {
            if (orderItem.getItemType() == ItemType.BOOK) {
                continue; // Physical books don't need digital enrollment
            }

            switch (orderItem.getItemType()) {
                case EBOOK -> {
                    Enrollment enrollment = createBaseEnrollment(student, orderItem.getItemType());
                    Ebook ebook = ebookRepository.findById(orderItem.getItemId())
                            .orElseThrow(() -> new ResourceNotFoundException("Ebook not found"));
                    enrollment.setEbook(ebook);
                    enrollments.add(enrollment);
                }
                case TESTSERIES -> {
                    Enrollment enrollment = createBaseEnrollment(student, orderItem.getItemType());
                    TestSeries testSeries = testSeriesRepository.findById(orderItem.getItemId())
                            .orElseThrow(() -> new ResourceNotFoundException("TestSeries not found"));
                    enrollment.setTestSeries(testSeries);
                    enrollments.add(enrollment);
                }
                case COURSE -> {
                    Enrollment enrollment = createBaseEnrollment(student, orderItem.getItemType());
                    CourseAttempt courseAttempt = courseAttemptRepository.findById(orderItem.getItemId())
                            .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
                    enrollment.setCourseAttempt(courseAttempt);
                    if (orderItem.getCourseBatchId() != null) {
                        CourseBatch courseBatch = courseBatchRepository
                                .findById(orderItem.getCourseBatchId())
                                .orElse(null);
                        if (courseBatch != null) {
                            enrollment.setCourseBatch(courseBatch);
                        }
                    }
                    enrollments.add(enrollment);
                }
                case COURSEBUNDLE -> {
                    // Create one enrollment per course attempt inside the bundle
                    CourseBundle courseBundle = courseBundleRepository.findById(orderItem.getItemId())
                            .orElseThrow(() -> new ResourceNotFoundException("Course-Bundle not found"));
                    for (CourseAttempt attempt : courseBundle.getCourseAttempts()) {
                        Enrollment attemptEnrollment = createBaseEnrollment(student, ItemType.COURSE);
                        attemptEnrollment.setCourseAttempt(attempt);
                        enrollments.add(attemptEnrollment);
                    }
                    // No base enrollment for COURSEBUNDLE — only individual course enrollments
                }
                default -> logger.warn("Unhandled item type: {} for order item: {}",
                        orderItem.getItemType(), orderItem.getId());
            }
        }

        if (!enrollments.isEmpty()) {
            enrollmentRepository.saveAll(enrollments);
            logger.info("Created {} enrollment(s) for order {}", enrollments.size(), order.getRandomId());
        }

        // 3. Save order (after all mutations)
        orderRepository.save(order);

        // 4. Clear the student's cart (null-safe)
        Cart cart = cartRepository.findByStudentId(student.getId()).stream().findFirst().orElse(null);
        if (cart != null) {
            cartRepository.delete(cart);
        }

        // 5. Send order confirmation email
        try {
            String emailBody = generateEmailContent(order);
            emailService.sendOrderConfirmationEmail(student.getEmail(), "Order Confirmation", emailBody);
            logger.info("Order confirmation email sent for order {}", order.getRandomId());
        } catch (Exception e) {
            // Email failure should not roll back the entire payment processing
            logger.error("Failed to send order confirmation email for order {}: {}",
                    order.getRandomId(), e.getMessage(), e);
        }

        // 6. Mark coupon as used (if applicable)
        Coupon coupon = order.getCoupon();
        if (coupon != null) {
            coupon.setUsed(true);
            couponRepository.save(coupon);
        }
    }

    /**
     * Creates a base {@link Enrollment} with common fields pre-populated.
     */
    private Enrollment createBaseEnrollment(Student student, ItemType itemType) {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setItemType(itemType);
        enrollment.setEnrolledAt(new Date());
        return enrollment;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Razorpay Webhook
    // ──────────────────────────────────────────────────────────────────────────

    @Operation(summary = "Webhook for Razorpay Payment")
    @PostMapping("/webhook")
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> handleRazorpayWebhook(@RequestBody String payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> event = objectMapper.readValue(payload, Map.class);

            String eventType = (String) event.get("event");
            logger.info("Received Razorpay webhook event: {}", eventType);

            // Only process successful payment events
            if (!"payment.captured".equals(eventType) && !"order.paid".equals(eventType)) {
                logger.info("Ignoring webhook event: {}", eventType);
                return ResponseEntity.ok("Event ignored: " + eventType);
            }

            // Extract payload data
            Map<String, Object> payloadData = (Map<String, Object>) event.get("payload");
            if (payloadData == null) {
                logger.warn("Webhook payload data is null");
                return ResponseEntity.badRequest().body("Invalid payload structure");
            }

            // Extract payment entity & payment ID
            Map<String, Object> payment = (Map<String, Object>) payloadData.get("payment");
            Map<String, Object> paymentEntity = payment != null
                    ? (Map<String, Object>) payment.get("entity") : null;
            if (paymentEntity == null) {
                logger.warn("Payment entity missing in webhook payload");
                return ResponseEntity.badRequest().body("Payment details missing in webhook payload");
            }

            String razorpayPaymentId = (String) paymentEntity.get("id");
            String razorpayOrderId = (String) paymentEntity.get("order_id");
            logger.info("Webhook payment ID: {}, Razorpay order ID: {}", razorpayPaymentId, razorpayOrderId);

            // Extract order entity & receipt (our internal order UUID)
            Map<String, Object> orderMap = (Map<String, Object>) payloadData.get("order");
            Map<String, Object> orderEntity = orderMap != null
                    ? (Map<String, Object>) orderMap.get("entity") : null;
            if (orderEntity == null) {
                logger.warn("Order entity missing in webhook payload");
                return ResponseEntity.badRequest().body("Order details missing in webhook payload");
            }

            String receipt = (String) orderEntity.get("receipt");
            if (receipt == null || receipt.isBlank()) {
                logger.warn("Receipt (internal order ID) is null or blank in webhook");
                return ResponseEntity.badRequest().body("Order ID not found in payment payload");
            }
            logger.info("Extracted receipt (internal order ID): {}", receipt);

            // Look up the order
            Optional<Order> orderOptional = orderRepository.findById(UUID.fromString(receipt));
            if (orderOptional.isEmpty()) {
                logger.error("Order not found for receipt: {}", receipt);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
            }

            Order order = orderOptional.get();

            // Idempotency: if order is already completed, return 200 OK
            // (Razorpay retries on non-2xx; returning 4xx would cause infinite retries)
            if (!"PENDING".equals(order.getStatus())) {
                logger.info("Order {} already processed (status: {}). Returning OK for idempotency.",
                        order.getRandomId(), order.getStatus());
                return ResponseEntity.ok("Order already processed.");
            }

            // Store Razorpay order ID
            order.setRazorPayId(razorpayOrderId);

            // Resolve student
            Student student = order.getStudent();
            if (student == null) {
                logger.error("Student is null on order {}", order.getRandomId());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Student not found for order");
            }

            // Delegate to shared processing logic
            processSuccessfulPayment(order, student, razorpayPaymentId);

            logger.info("Webhook processing completed successfully for order {}", order.getRandomId());
            return ResponseEntity.ok("Payment successful. Order processed and enrollments created.");

        } catch (Exception e) {
            logger.error("Error processing Razorpay webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook: " + e.getMessage());
        }
    }

    @Operation(summary = "Get Order By ID")
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderByOrderId(@PathVariable UUID orderId) {
        Map<String, Object> response = new HashMap<>();
        try {
            OrderDetailsDto orderDetails = orderService.getOrderByOrderId(orderId);
            response.put("data", orderDetails);
            response.put("message", "Order fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
            return ResponseEntity.status(ex.getStatusCode()).body(response);
        }
    }

    @Operation(summary = "Get All Orders")
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllOrders(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<OrderDetailsDto> ordersPage = orderService.getAllOrders(pageable);

            response.put("meta", ordersPage.getContent());
            response.put("currentPage", ordersPage.getNumber() + 1);
            response.put("totalItems", ordersPage.getTotalElements());
            response.put("totalPages", ordersPage.getTotalPages());
            response.put("message", "Orders fetched successfully");
            response.put("error", false);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Get Orders By Student ID")
    @GetMapping("/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getOrdersByStudentId(@PathVariable UUID studentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<OrderDetailsDto> ordersPage = orderService.getOrdersByStudentId(studentId, pageable);

            response.put("meta", ordersPage.getContent());
            response.put("currentPage", ordersPage.getNumber() + 1);
            response.put("totalItems", ordersPage.getTotalElements());
            response.put("totalPages", ordersPage.getTotalPages());
            response.put("message", "Orders fetched successfully");
            response.put("error", false);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }


    @Operation(summary = "Get Orders By Student email")
    @GetMapping("/student/email")
    public ResponseEntity<Map<String, Object>> getOrdersByStudentEmail(@RequestParam String email,
                                                                       @RequestHeader(value = "subdomain", required = false) String subdomain,
                                                                    @RequestParam(defaultValue = "1") int page,
                                                                    @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> response = new HashMap<>();
        try {
            UUID tenantId = authUtil.validateAndGetUser(subdomain);
            Student student = studentRepository.findByTenantIdAndEmail(tenantId,email);
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<OrderDetailsDto> ordersPage = orderService.getAllOrdersByStudentId(student.getId(), pageable);

            response.put("meta", ordersPage.getContent());
            response.put("currentPage", ordersPage.getNumber() + 1);
            response.put("totalItems", ordersPage.getTotalElements());
            response.put("totalPages", ordersPage.getTotalPages());
            response.put("message", "Orders fetched successfully");
            response.put("error", false);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Get My Learning by Student ID")
    @GetMapping("/my-learning/{studentId}")
    public ResponseEntity<ApiResponseWithMeta> getMyLearning(
            @PathVariable UUID studentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filter) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));

        Sort sort;

        if ("orders".equalsIgnoreCase(filter)) {
            sort = Sort.by("createdAt").descending(); // ✅ Order entity field
        } else {
            sort = Sort.by("enrolledAt").descending(); // ✅ Enrollment entity field
        }

        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<EnrollmentDTO> enrollmentPage;

        if (filter != null && !filter.isBlank()) {

            // ✅ NEW FILTER: ORDERS
            if (filter.equalsIgnoreCase("orders")) {

                Page<OrderDetailsDto> orderPage = orderService.getOrdersByStudentId(studentId, pageable);

                Map<String, Object> meta = new HashMap<>();
                meta.put("totalOrders", orderPage.getTotalElements());
                meta.put("totalPages", orderPage.getTotalPages());
                meta.put("currentPage", orderPage.getNumber() + 1);
                meta.put("pageSize", orderPage.getSize());
                meta.put("studentId", studentId);
                meta.put("studentName", student.getName());
                meta.put("studentEmail", student.getEmail());
                meta.put("studentPhone", student.getPhoneNumber());

                ApiResponseWithMeta response = new ApiResponseWithMeta(
                        orderPage.getContent().isEmpty() ? "No orders found." : "Orders retrieved successfully.",
                        false,
                        Map.of("orders", orderPage.getContent(), "meta", meta));

                return ResponseEntity.ok(response);
            }

            ItemType itemType = null;
            switch (filter.toLowerCase()) {
                case "test":
                    itemType = ItemType.TESTSERIES;
                    break;
                case "ebook":
                    itemType = ItemType.EBOOK;
                    break;
                case "course":
                    itemType = ItemType.COURSE;
                    break;
                default:
                    // If the filter is unrecognized, we simply ignore it and return all records.
                    break;
            }
            if (itemType != null) {
                enrollmentPage = orderService.getEnrolledItemsByType(studentId, itemType, pageable);
            } else {
                enrollmentPage = orderService.getEnrolledItems(studentId, pageable);
            }
        } else {
            enrollmentPage = orderService.getEnrolledItems(studentId, pageable);
        }

        Map<String, Object> meta = new HashMap<>();
        meta.put("totalEnrolled", enrollmentPage.getTotalElements());
        meta.put("totalPages", enrollmentPage.getTotalPages());
        meta.put("currentPage", enrollmentPage.getNumber() + 1);
        meta.put("pageSize", enrollmentPage.getSize());
        meta.put("studentId", studentId);
        meta.put("studentName", student.getName());
        meta.put("studentEmail", student.getEmail());
        meta.put("studentPhone", student.getPhoneNumber());

        ApiResponseWithMeta response = new ApiResponseWithMeta(
                enrollmentPage.getContent().isEmpty() ? "No enrolled items found."
                        : "Enrolled items retrieved successfully.",
                false,
                Map.of("enrolledItems", enrollmentPage.getContent(), "meta", meta));

        return ResponseEntity.ok(response);
    }

}