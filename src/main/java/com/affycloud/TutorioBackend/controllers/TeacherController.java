package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.admin.AdminDetailDto;
import com.affycloud.TutorioBackend.dtos.forgetPassword.ForgetPasswordDto;
import com.affycloud.TutorioBackend.dtos.forgetPassword.ForgetPasswordRequestDto;
import com.affycloud.TutorioBackend.dtos.forgetPassword.VerifyOtpRequestDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.teachers.TeacherAddDto;
import com.affycloud.TutorioBackend.dtos.teachers.TeacherDetailDto;
import com.affycloud.TutorioBackend.dtos.teachers.TeacherUpdateDto;
import com.affycloud.TutorioBackend.handler.IncorrectOldPasswordException;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.repositories.TeacherRepository;
import com.affycloud.TutorioBackend.services.EmailService;
import com.affycloud.TutorioBackend.services.OtpService;
import com.affycloud.TutorioBackend.services.TeacherService;
import com.affycloud.TutorioBackend.utils.AuthUtilAdmin;
import com.affycloud.TutorioBackend.utils.PasswordUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    @Autowired
    private TeacherService adminService;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordUtils passwordUtils;

    @Autowired
    private AuthUtilAdmin authUtil;

    @Operation(summary = "Create a Teacher")
    @PostMapping("create")
    public ResponseEntity<Map<String, Object>> createTeacher(@RequestBody TeacherAddDto request,
                                                             HttpServletRequest httpRequest) {

        User parent = authUtil.getAuthenticatedUser(httpRequest);
        Map<String, Object> response = adminService.createTeacher(request,parent);
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

    @Operation(summary = "Update existing teacher")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTeacher(@PathVariable UUID id, @RequestBody TeacherUpdateDto request) {
        Map<String, Object> response = adminService.updateTeacher(id, request);
        Object value = response.values().iterator().next();

        if (value.getClass() == AdminDetailDto.class) {
            return ResponseEntity.ok().body(response);
        } else {
            return ResponseEntity.status(HttpStatus.IM_USED).body(response);
        }
    }

    @Operation(summary = "Get Teacher by ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable UUID id) {
        Map<String, Object> response = new HashMap<>();
        try {
            TeacherDetailDto admin = adminService.getTeacherById(id);
            response.put("meta", admin);  // "meta" contains the admin details
            response.put("message", "Teacher fetched successfully");
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", "true");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Get all Teachers ")
    @GetMapping("/all")
    public ResponseEntity<?> getAllTeachers(@RequestParam(value = "status", required = false) Boolean status) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<TeacherDetailDto> admins; ;
            // Agar koi filter parameter provide hua hai to filtered search, warna all courses
            if (status != null ) {
                admins = adminService.getAllTeachersByStatus(status);
            } else {
                admins = adminService.getAllTeachers();
            }
            response.put("meta", admins);  // "meta" contains the admins details
            response.put("message", "Teacher fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Get all Teachers By Parent")
    @GetMapping("/all-by-parent")
    public ResponseEntity<?> getAllTeachersByParent(@RequestParam(value = "status", required = false) Boolean status,
                                                   HttpServletRequest httpRequest,
                                                   @RequestParam(value = "tenantId", required = false) UUID tenantId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<TeacherDetailDto> admins;
            // Agar koi filter parameter provide hua hai to filtered search, warna all courses
            if (tenantId != null ) {
                Optional<User> user = teacherRepository.findById(tenantId);
                if (user.isPresent()) {
                    admins = adminService.getAllTeachersByParent(user.get());
                } else {
                    response.put("message", "Parent not found");
                    response.put("error", true);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
            } else {
                User user = authUtil.getAuthenticatedUser(httpRequest);
                admins = adminService.getAllTeachersByParent(user);
            }
            response.put("meta", admins);  // "meta" contains the admins details
            response.put("message", "Teacher fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }



    @Operation(summary = "Reset the Teacher's password")
    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @PathVariable UUID id,
            @RequestBody ForgetPasswordDto forgetPasswordDto) {
        try {
            // Old and new password ko ForgetPasswordDto se lete hain
            adminService.resetPassword(id,forgetPasswordDto);
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

    @Operation(summary = "send otp forget password admin")
    @PostMapping("/forget-password")
    public ResponseEntity<ApiResponse> forgetPassword(@RequestBody ForgetPasswordRequestDto request) {
        User admin = teacherRepository.findByEmail(request.getEmail());
        if (admin != null) {
            String otp = otpService.generateOtp();
            otpService.storeOtp(request.getEmail(), otp);
            emailService.sendOtp(request.getEmail(), otp);
            ApiResponse response = new ApiResponse("OTP sent to your email.", false);
            return ResponseEntity.ok(response);
        } else {
            ApiResponse response = new ApiResponse("Teacher not found.", true);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(summary = "Verify OTP and reset password of admin")
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@RequestBody VerifyOtpRequestDto request) {
        String cachedOtp = otpService.getOtp(request.getEmail());

        if (cachedOtp != null && cachedOtp.equals(request.getOtp())) {
            User admin = teacherRepository.findByEmail(request.getEmail());
            if (admin != null) {
                admin.setPassword(passwordUtils.encryptPassword(request.getNewPassword()));
                teacherRepository.save(admin);
                otpService.invalidateOtp(request.getEmail());
                ApiResponse response = new ApiResponse("Password updated successfully.", false);
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

    @Operation(summary = "Toggle Teacher Active Status")
    @PutMapping("/toggle-active/{teacherId}")
    public ResponseEntity<?> toggleIsActive(@PathVariable UUID teacherId) {
        Map<String, Object> response = adminService.toggleIsActive(teacherId);
        if ((boolean) response.getOrDefault("error", true)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

}
