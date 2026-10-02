package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.admin.AdminAddDto;
import com.affycloud.TutorioBackend.dtos.admin.AdminDetailDto;
import com.affycloud.TutorioBackend.dtos.admin.AdminUpdateDto;
import com.affycloud.TutorioBackend.dtos.admin.RazorpayConfigRequest;
import com.affycloud.TutorioBackend.dtos.forgetPassword.ForgetPasswordDto;
import com.affycloud.TutorioBackend.dtos.forgetPassword.ForgetPasswordRequestDto;
import com.affycloud.TutorioBackend.dtos.forgetPassword.VerifyOtpRequestDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.handler.IncorrectOldPasswordException;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.repositories.AdminRepository;
import com.affycloud.TutorioBackend.services.*;
import com.affycloud.TutorioBackend.utils.AuthUtilAdmin;
import com.affycloud.TutorioBackend.utils.PasswordUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordUtils passwordUtils;

    @Autowired
    private AuthUtilAdmin authUtil;

    @Operation(summary = "Create an admin")
    @PostMapping("create")
    public ResponseEntity<Map<String, Object>> createAdmin(@RequestBody AdminAddDto request) {
        Map<String, Object> response = adminService.createAdmin(request);
        /* Storing value from the response. */
        Object value = response.get("response");
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


    @Operation(summary = "Update existing admin")
    @PutMapping
    public ResponseEntity<ApiResponse> updateAdmin(
            @Valid @RequestBody AdminUpdateDto request,
            HttpServletRequest httpRequest) {

        User user = authUtil.getAuthenticatedUser(httpRequest);
        ApiResponse response = adminService.updateAdmin(user.getId(), request);
        return ResponseEntity.status(response.isError() ? HttpStatus.CONFLICT : HttpStatus.OK).body(response);
    }


    @Operation(summary = "Get Admin by ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable UUID id,
                                          HttpServletRequest httpRequest) {
        User user = authUtil.getAuthenticatedUser(httpRequest);

        UUID userId = user.getId();

        if (id != null) {
            userId = id;
        }
            Map<String, Object> response = new HashMap<>();
        try {
            AdminDetailDto admin = adminService.getAdminById(userId);
            response.put("meta", admin);  // "meta" contains the admin details
            response.put("message", "Admin fetched successfully");
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", "true");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Check organisation name availability")
    @GetMapping("/check-org-name")
    public ResponseEntity<ApiResponse> checkOrganisationNameAvailability(@RequestParam("organisationName") String organisationName) {
        ApiResponse response = adminService.checkOrganisationNameAvailability(organisationName);
        return ResponseEntity.status(response.isError() ? HttpStatus.CONFLICT : HttpStatus.OK).body(response);
    }

    @Operation(summary = "Check Landing Page availability")
    @GetMapping("/check-landing-page")
    public ResponseEntity<Map<String, Object>> checkLandingPageAvailability(
            @RequestHeader(value = "subdomain", required = true) String subdomain
    ) {

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Result fetched successfully");
        if(Objects.equals(subdomain, "meta-code")) response.put("landingPage", false); else response.put("landingPage", true);
        response.put("error", false);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all Admins ")
    @GetMapping("/all")
    public ResponseEntity<?> getAllAdmins() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<AdminDetailDto> admins = adminService.getAllAdmins();
            response.put("meta", admins);  // "meta" contains the admins details
            response.put("message", "Admins fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Reset the Admin's password")
    @PatchMapping("/{id}/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @PathVariable UUID id,
            @RequestBody ForgetPasswordDto passwordResetRequest) {
        try {
            adminService.resetPassword(id, passwordResetRequest);
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

    @Operation(summary = "Send otp forget password admin")
    @PostMapping("/forget-password")
    public ResponseEntity<ApiResponse> forgetPassword(@RequestBody ForgetPasswordRequestDto request) {
        User admin = adminRepository.findByEmail(request.getEmail());
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
            User admin = adminRepository.findByEmail(request.getEmail());

            if (admin != null) {
                admin.setPassword(passwordUtils.encryptPassword(request.getNewPassword()));
                adminRepository.save(admin);
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

    @PostMapping("/payment/razorpay")
    public ResponseEntity<ApiResponse> updateRazorpayConfig(
            HttpServletRequest request,
            @RequestBody @Valid RazorpayConfigRequest dto) {

        User user = authUtil.getAuthenticatedUser(request);
        ApiResponse response =
                adminService.updateRazorpayConfig(user, dto);

        return ResponseEntity.ok(response);
    }

}
