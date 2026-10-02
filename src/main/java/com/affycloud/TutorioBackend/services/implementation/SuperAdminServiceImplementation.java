package com.affycloud.TutorioBackend.services.implementation;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.affycloud.TutorioBackend.dtos.login.LoginDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.dtos.student.StudentDetailDto;
import com.affycloud.TutorioBackend.dtos.superAdmin.SuperAdminDetailDto;
import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.repositories.StudentRepository;
import com.affycloud.TutorioBackend.repositories.SuperAdminRepository;
import com.affycloud.TutorioBackend.services.SuperAdminService;
import com.affycloud.TutorioBackend.utils.JwtUtil;
import com.affycloud.TutorioBackend.utils.PasswordUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.PostConstruct;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SuperAdminServiceImplementation implements SuperAdminService {
    @Autowired
    private SuperAdminRepository repository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordUtils passwordUtils;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @PostConstruct
    public void createSuperAdminAccount() {

        User superAdminAccount = repository.findByRole(Role.SUPER_ADMIN);

        if (superAdminAccount == null) {
            User superAdmin = new User();
            superAdmin.setEmail("superadmin@test.com");
            superAdmin.setName("Super Admin");
            superAdmin.setPhoneNumber("7725050222");
            superAdmin.setRole(Role.SUPER_ADMIN);
            superAdmin.setUsername("super_admin");
            superAdmin.setPassword(passwordUtils.encryptPassword("123456")); // Encrypt password
            repository.save(superAdmin);
        }
    }

    @Override
    public ApiResponseWithMeta authenticateSuperAdmin(LoginDto request) {
        User user = repository.findByEmail(request.getEmail());

        if (user == null) {
            return new ApiResponseWithMeta("User Not Found", true, null);
        }

        if (!user.getIsActive()) {
            return new ApiResponseWithMeta("Your Account is not Active", true, null);
        }

        String requestPassword = request.getPassword();
        if (requestPassword == null || requestPassword.isEmpty()) {
            return new ApiResponseWithMeta("Password cannot be null or empty", true, null);
        }

        String userPassword = user.getPassword();
        if (userPassword == null || userPassword.isEmpty()) {
            return new ApiResponseWithMeta("Stored password is null or empty for the user", true, null);
        }

        BCrypt.Result result = BCrypt.verifyer().verify(requestPassword.toCharArray(), userPassword);
        if (!result.verified) {
            return new ApiResponseWithMeta("Invalid Password", true, null);
        }

        // Successful authentication
        SuperAdminDetailDto superAdminDetailDto = modelMapper.map(user, SuperAdminDetailDto.class);
        String accessToken = jwtUtil.generateAccessTokenForAdmin(user.getEmail(), superAdminDetailDto);
        String refreshToken = jwtUtil.generateRefreshTokenForAdmin(user.getEmail());

        Map<String, Object> meta = new HashMap<>();
        meta.put("accessToken", accessToken);
        meta.put("refreshToken", refreshToken);
        meta.put("role", superAdminDetailDto.getRole());

        return new ApiResponseWithMeta("Admin login successfully", false, meta);
    }

    @Override
    public Map<String, Object> refreshToken(String refreshToken) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = jwtUtil.extractUsername(refreshToken);

            // Find user
            User customer = repository.findByEmail(email);
            if (customer == null) {
                response.put("error", true);
                response.put("message", "User not found.");
                return response;
            }

            // Check expiration
            if (jwtUtil.isTokenExpired(refreshToken)) {
                response.put("error", true);
                response.put("message", "Refresh token expired.");
                return response;
            }

            // Generate tokens
            SuperAdminDetailDto dto = modelMapper.map(customer, SuperAdminDetailDto.class);
            String newAccess = jwtUtil.generateAccessTokenForAdmin(email, dto);
            String newRefresh = jwtUtil.generateRefreshTokenForAdmin(email);

            Map<String, Object> meta = new HashMap<>();
            meta.put("accessToken", newAccess);
            meta.put("refreshToken", newRefresh);

            response.put("meta", meta);
            response.put("message", "Token refreshed successfully");
            response.put("error", false);

        } catch (ExpiredJwtException e) {

            // ✔ SIMPLE ERROR MESSAGE HERE
            response.put("error", true);
            response.put("message", "Refresh token expired.");

        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Something went wrong while refreshing token.");
        }

        return response;
    }

    @Override
    public Map<String, Object> refreshStudentToken(String refreshToken) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = jwtUtil.extractUsername(refreshToken);
            String  tenantIdString = jwtUtil.extractTenantId(refreshToken);
            UUID tenantId = UUID.fromString(tenantIdString);

            // Find user
            Student student = studentRepository.findByTenantIdAndEmail(tenantId,email);
            if (student == null) {
                response.put("error", true);
                response.put("message", "Student not found.");
                return response;
            }

            // Check expiration
            if (jwtUtil.isTokenExpired(refreshToken)) {
                response.put("error", true);
                response.put("message", "Refresh token expired.");
                return response;
            }

            // Generate tokens
            StudentDetailDto dto = modelMapper.map(student, StudentDetailDto.class);
            String newAccess = jwtUtil.generateAccessTokenForStudent(tenantId,email, dto);
            String newRefresh = jwtUtil.generateRefreshTokenForStudent(tenantId,email);

            Map<String, Object> meta = new HashMap<>();
            meta.put("accessToken", newAccess);
            meta.put("refreshToken", newRefresh);

            response.put("meta", meta);
            response.put("message", "Token refreshed successfully");
            response.put("error", false);

        } catch (ExpiredJwtException e) {

            // ✔ SIMPLE ERROR MESSAGE HERE
            response.put("error", true);
            response.put("message", "Refresh token expired.");

        } catch (Exception e) {
            response.put("error", true);
            response.put("message", "Something went wrong while refreshing token.");
        }

        return response;
    }




}
