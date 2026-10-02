package com.affycloud.TutorioBackend.controllers;


import com.affycloud.TutorioBackend.dtos.forgetPassword.ForgetPasswordDto;
import com.affycloud.TutorioBackend.dtos.forgetPassword.ForgetPasswordRequestDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.dtos.student.StudentAddDto;
import com.affycloud.TutorioBackend.dtos.student.StudentDetailDto;
import com.affycloud.TutorioBackend.dtos.student.StudentUpdateDto;
import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.handler.IncorrectOldPasswordException;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.repositories.StudentRepository;
import com.affycloud.TutorioBackend.repositories.TeacherRepository;
import com.affycloud.TutorioBackend.services.EmailService;
import com.affycloud.TutorioBackend.services.OtpService;
import com.affycloud.TutorioBackend.services.SmsService;
import com.affycloud.TutorioBackend.services.StudentService;
import com.affycloud.TutorioBackend.utils.AuthUtilAdmin;
import com.affycloud.TutorioBackend.utils.JwtUtil;
import com.affycloud.TutorioBackend.utils.PasswordUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


@RequestMapping("/api/student")
@RestController
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private AuthUtilAdmin authUtil;

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);


    @Operation(summary = "Create a Student")
    @PostMapping("create")
    public ResponseEntity<Map<String, Object>> createStudent(@RequestBody StudentAddDto request,
                                                             HttpServletRequest httpRequest)  {
        User user = authUtil.getAuthenticatedUser(httpRequest);
        Map<String, Object> response = studentService.createStudent(request,user);
        
        if (Boolean.TRUE.equals(response.get("error"))) {
            String errorMessage = response.get("message") != null ? String.valueOf(response.get("message")) : "";
            if (errorMessage.contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "update existing student")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateStudent(@PathVariable UUID id,
                                                             @RequestBody StudentUpdateDto request,
                                                             HttpServletRequest httpRequest) {
        ResponseEntity<Map<String, Object>> denial = denyIfNotAllowedToUpdate(id, httpRequest);
        if (denial != null) {
            return denial;
        }

        Map<String, Object> response = studentService.updateStudent(id, request);

        if (Boolean.TRUE.equals(response.get("error"))) {
            String errorMessage = response.get("message") != null ? String.valueOf(response.get("message")) : "";
            if (errorMessage.contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Guards {@code PUT /api/student/{id}} so the row being written is one the
     * caller is actually allowed to write.
     *
     * <p>The id used to be taken from the URL and trusted. A student panel that
     * still held a previous session's id in memory therefore wrote its current
     * owner's name and phone number onto a stranger's row, and the API happily
     * accepted it. The stranger kept the number; the real owner's row stayed
     * empty, and their next attempt to save the number at checkout was rejected
     * as a duplicate - by their own number, stored on somebody else.
     *
     * <p>A student may only update themselves. Institute staff (ADMIN /
     * TEACHER) may update students inside their own tenant, and SUPER_ADMIN
     * anyone.
     *
     * @return {@code null} when the update may proceed, otherwise the error
     *         response to return.
     */
    private ResponseEntity<Map<String, Object>> denyIfNotAllowedToUpdate(UUID id, HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();

        Student authenticatedStudent = authUtil.getOptionalAuthenticatedStudent(httpRequest);
        if (authenticatedStudent != null) {
            if (authenticatedStudent.getId().equals(id)) {
                return null;
            }
            logger.warn("Blocked profile update of student {} attempted with the session of student {}",
                    id, authenticatedStudent.getId());
            response.put("message", "You can only update your own profile.");
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        User staff = authUtil.getOptionalAuthenticatedUser(httpRequest);
        if (staff == null) {
            response.put("message", "You must be signed in to update a profile.");
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (staff.getRole() == Role.SUPER_ADMIN) {
            return null;
        }

        UUID tenantId = staff.getRole() == Role.TEACHER
                ? (staff.getParent() == null ? null : staff.getParent().getId())
                : staff.getId();

        if (tenantId != null && studentRepository.findByTenantIdAndId(tenantId, id).isPresent()) {
            return null;
        }

        logger.warn("Blocked profile update of student {} by user {} outside their tenant", id, staff.getId());
        response.put("message", "You are not allowed to update this profile.");
        response.put("error", true);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @Operation(summary = "profile setup of student")
    @PutMapping("/profile-setup")
    public ResponseEntity<Map<String, Object>> profileSetupStudent(@RequestBody StudentUpdateDto request,
                                                                   HttpServletRequest httpRequest)  {
        Student student = authUtil.getAuthenticatedStudent(httpRequest);
        Map<String, Object> response = studentService.updateStudent(student.getId(), request);

        if (Boolean.TRUE.equals(response.get("error"))) {
            String errorMessage = response.get("message") != null ? String.valueOf(response.get("message")) : "";
            if (errorMessage.contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }


    @Operation(summary = "get student by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStudentById(@PathVariable UUID id) {
        Map<String, Object> response = new HashMap<>();

        try {
            StudentDetailDto student = studentService.getStudentById(id);
            response.put("meta", student);  // "meta" contains the admin details
            response.put("message", "Student fetched successfully");
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", "true");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Get all students")
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
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
            Page<StudentDetailDto> studentPage = studentService.getAllStudents(page, size,id);
            response.put("totalItems", studentPage.getTotalElements());
            response.put("meta", studentPage.getContent());
            response.put("totalPages", studentPage.getTotalPages());
            response.put("currentPage", studentPage.getNumber());
            response.put("message", "Students fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            response.put("message", "Something went wrong while fetching students");
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Search students by keyword (name, phone, email, country code)")
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchStudents(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest){

        User user = authUtil.getAuthenticatedUser(httpRequest);
        Map<String, Object> response = new HashMap<>();

        try {
            Page<StudentDetailDto> studentPage;
            if(user.getRole() == Role.ADMIN) {
              studentPage = studentService.searchStudents(keyword, page, size, user.getId());
            }
            else {
                studentPage = studentService.searchStudents(keyword, page, size, user.getParent().getId());
            }
            response.put("totalItems", studentPage.getTotalElements());
            response.put("meta", studentPage.getContent());
            response.put("totalPages", studentPage.getTotalPages());
            response.put("currentPage", studentPage.getNumber() + 1);
            response.put("message", "Students searched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            response.put("message", "Something went wrong while searching students");
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Delete Student")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteStudent(@PathVariable UUID id) {
    try {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(new ApiResponse("Student deleted successfully.", false));
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(e.getMessage(), true));
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("An error occurred while deleting the student: " + e.getMessage(), true));
    }
    }


    @Operation(summary = "Reset the Student's password")
    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @PathVariable UUID id,
            @RequestBody ForgetPasswordDto forgetPasswordDto) {
        try {
            // Old and new password ko ForgetPasswordDto se lete hain
            studentService.resetPassword(id, forgetPasswordDto);
            ApiResponse response = new ApiResponse("Password updated successfully", false);
            return ResponseEntity.ok(response);
        } catch (IncorrectOldPasswordException ex) {
            ApiResponse response = new ApiResponse(ex.getMessage(), true);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // 409 Conflict
        } catch (IllegalArgumentException ex) {
            ApiResponse response = new ApiResponse(ex.getMessage(), true);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // 400 Bad Request
        }
    }

    @Operation(summary = "send otp for forget password student")
    @PostMapping("/forget-password")
    public ResponseEntity<ApiResponse> forgetPassword(@RequestBody ForgetPasswordRequestDto request,@RequestParam UUID tenantId) {
        Student student = studentRepository.findByTenantIdAndEmail(tenantId, request.getEmail());
        if (student != null) {
            String otp = otpService.generateOtp();
            otpService.storeOtp(request.getEmail(), otp);
            emailService.sendOtp(request.getEmail(), otp);
            ApiResponse response = new ApiResponse("OTP sent to your email.", false);
            return ResponseEntity.ok(response);

        } else {
            ApiResponse response = new ApiResponse("Student not found.", true);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(summary = "Send OTP for phone number verification")
    @PostMapping("/otp/send")
    public ResponseEntity<?> sendOtp(@RequestParam("phoneNumber") String phoneNumber,@RequestParam UUID tenantId) {
        try {
                // Check if a student already exists with this phone number
                if (studentRepository.existsByTenantIdAndPhoneNumber(tenantId,phoneNumber)) {
                    logger.warn("OTP request failed: phone number already registered - {}", phoneNumber);
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new ApiResponseWithMeta("Student already exists with this phone number.", true, null));
                }

            // Check if OTP already exists in cache for this number
            String existingOtp = otpService.getOtp(phoneNumber);
            if (existingOtp != null) {
                logger.info("OTP already sent for phone number: {}", phoneNumber);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new ApiResponseWithMeta("OTP already sent. Please wait before requesting again.", true, null));
            }

                // Generate and store OTP
                String otp = otpService.generateOtp();
                otpService.storeOtp(phoneNumber, otp);

                // Send OTP via SMS
                String smsResponse = smsService.sendOtp(phoneNumber, otp);
                logger.info("OTP sent to phone number: {}, SMS service response: {}", phoneNumber, smsResponse);

                Map<String, Object> meta = new HashMap<>();
                meta.put("otp", otp);
                meta.put("OTP sent to phone number", phoneNumber);
                return ResponseEntity.ok(new ApiResponseWithMeta("OTP sent to your phone.", false, meta));
            } catch (Exception e) {
                logger.error("Error while sending OTP to phone number: {}", phoneNumber, e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponseWithMeta("Failed to send OTP. Please try again later.", true, null));
            }
        }


    @Operation(summary = "Resend OTP to the given phone number")
    @PostMapping("/otp/resend")
    public ResponseEntity<?> resendOtp(@RequestParam("phoneNumber") String phoneNumber) {
        try {
            if (!otpService.canResendOtp(phoneNumber)) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(new ApiResponseWithMeta("Please wait a minute before requesting a new OTP.", true, null));
            }
            String otp = otpService.generateOtp();
            otpService.storeOtp(phoneNumber, otp);
            String smsResponse = smsService.sendOtp(phoneNumber, otp);
            logger.info("Resent OTP to {} | SMS response: {}", phoneNumber, smsResponse);
            Map<String, Object> meta = new HashMap<>();
            meta.put("phoneNumber", phoneNumber);

            return ResponseEntity.ok(new ApiResponseWithMeta("OTP resent successfully.", false, meta));
        } catch (Exception e) {
            logger.error("Error resending OTP to phone number: {}", phoneNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseWithMeta("Failed to resend OTP. Please try again later.", true, null));
        }
    }


}

    /*@Operation(summary = "Verify OTP and register new student")
    @PostMapping("/register/verify-otp")
    public ResponseEntity<?> registerStudent(@Valid @RequestBody StudentRegisterDto request,@RequestParam UUID tenantId) {
        // Retrieve the OTP stored against the phone number
        String cachedOtp = otpService.getOtp(request.getPhoneNumber());

        if (cachedOtp == null || !cachedOtp.equals(request.getOtp())) {
            logger.error("Invalid or expired OTP for phone number: {}", request.getPhoneNumber());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Invalid or expired OTP.", true));
        }

        // Check again if a student already exists with the provided phone number
        if (studentRepository.existsByTenantIdAndPhoneNumber(tenantId,request.getPhoneNumber())) {
            logger.warn("Student already exists with phone number: {}", request.getPhoneNumber());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse("Student already exists with this phone number.", true));
        }

        // Check again if a student already exists with the provided phone number
        if (studentRepository.existsByTenantIdAndEmail(tenantId,request.getEmail())) {
            logger.warn("Student already exists with email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse("Student already exists with this email.", true));
        }

        // Create and populate a new student entity
        Student student = new Student();
        student.setName(request.getName());
        student.setEmail(request.getEmail());
        student.setPhoneNumber(request.getPhoneNumber());
        student.setPassword(passwordUtils.encryptPassword(request.getNewPassword()));
        student.setRole(Role.STUDENT);
        // Persist the student in the database

        // Invalidate the OTP after successful registration
        otpService.invalidateOtp(request.getPhoneNumber());

        // Generate JWT token after registration.
        // Assuming you have a JwtUtil bean and ModelMapper to convert your Student to StudentDetailDto.
        StudentDetailDto studentDetailDto = modelMapper.map(student, StudentDetailDto.class);
        String token = jwtUtil.generateAccessTokenForStudent(student.getEmail(), studentDetailDto);

        String refreshToken = jwtUtil.generateRefreshToken(student.getEmail());

        // Save refresh token to database
        student.setRefreshToken(refreshToken);
        studentRepository.save(student);

        // Prepare meta data with the token
        Map<String, Object> meta = new HashMap<>();

        meta.put("token", token);
        meta.put("refreshToken", refreshToken);

        logger.info("New student registered successfully with phone number: {}", request.getPhoneNumber());
        return ResponseEntity.ok(new ApiResponseWithMeta("Student registered successfully.", false,meta));
    }
*/
    /*@Operation(summary = "Verify OTP and reset password of student")
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@RequestBody VerifyOtpRequestDto request,@RequestParam UUID tenantId) {
        String cachedOtp = otpService.getOtp(request.getEmail());

        if (cachedOtp != null && cachedOtp.equals(request.getOtp())) {
            Student student = studentRepository.findByTenantIdAndEmail(tenantId,request.getEmail());

            if (student != null) {
                student.setPassword(passwordUtils.encryptPassword(request.getNewPassword()));
                studentRepository.save(student);
                otpService.invalidateOtp(request.getEmail());
                ApiResponse response = new ApiResponse("Password updated successfully", false);
                return ResponseEntity.ok(response);

            } else {
                ApiResponse response = new ApiResponse("Student not found.", true);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } else {
            ApiResponse response = new ApiResponse("Invalid Otp.", true);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
*/