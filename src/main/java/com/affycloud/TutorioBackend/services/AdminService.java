package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.admin.AdminAddDto;
import com.affycloud.TutorioBackend.dtos.admin.AdminDetailDto;
import com.affycloud.TutorioBackend.dtos.admin.AdminUpdateDto;
import com.affycloud.TutorioBackend.dtos.admin.RazorpayConfigRequest;
import com.affycloud.TutorioBackend.dtos.forgetPassword.ForgetPasswordDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.models.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AdminService {
    Map<String, Object> createAdmin(AdminAddDto adminDto);
    ApiResponse updateAdmin(UUID id, AdminUpdateDto request);
    AdminDetailDto getAdminById(UUID id);
    ApiResponse checkOrganisationNameAvailability(String organisationName);
    List<AdminDetailDto> getAllAdmins();
    void deleteAdmin(UUID id);
    boolean resetPassword(UUID id, ForgetPasswordDto passwordResetRequest);
    ApiResponse updateRazorpayConfig(
            User user,
            RazorpayConfigRequest dto);

}
