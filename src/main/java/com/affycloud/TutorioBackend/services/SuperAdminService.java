package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.login.LoginDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;

import java.util.Map;

public interface SuperAdminService {
    void createSuperAdminAccount();
    ApiResponseWithMeta authenticateSuperAdmin(LoginDto request);
    Map<String, Object> refreshToken(String refreshToken);
    Map<String, Object> refreshStudentToken(String refreshToken);
}
