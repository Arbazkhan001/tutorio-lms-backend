package com.affycloud.TutorioBackend.utils;

import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.repositories.AdminRepository;
import com.affycloud.TutorioBackend.repositories.StudentRepository;
import com.affycloud.TutorioBackend.repositories.SuperAdminRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Component
public class AuthUtilAdmin {

    private final JwtUtil jwtUtil;
    private final SuperAdminRepository customerRepository;
    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public AuthUtilAdmin(JwtUtil jwtUtil, SuperAdminRepository customerRepository, AdminRepository adminRepository, StudentRepository studentRepository) {
        this.jwtUtil = jwtUtil;
        this.customerRepository = customerRepository;
        this.adminRepository = adminRepository;
        this.studentRepository = studentRepository;
    }

    public User getAuthenticatedUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null ) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT token is required.");
        }

        if (!authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT token format.");
        }

        String token = authHeader.substring(7);

        if (jwtUtil.isTokenExpired(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is expired.");
        }

        String email = jwtUtil.extractEmail(token);


        if (email == null || email.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email not found in token.");
        }

        User customer = customerRepository.findByEmail(email);

        if (customer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found.");
        }

        return customer;
    }

    public User getOptionalAuthenticatedUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        try {
            String token = authHeader.substring(7);

            if (jwtUtil.isTokenExpired(token)) {
                return null;
            }

            String email = jwtUtil.extractEmail(token);

            if (email == null || email.isEmpty()) {
                return null;
            }

            return customerRepository.findByEmail(email);
        } catch (Exception e) {
            return null;
        }
    }


    public Student getAuthenticatedStudent(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null ) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT token is required.");
        }

        if (!authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT token format.");
        }

        String token = authHeader.substring(7);

        if (jwtUtil.isTokenExpired(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token is expired.");
        }

        String email = jwtUtil.extractEmail(token);
        String tenantIdString = jwtUtil.extractTenantId(token);

        UUID tenantId;
        try {
            tenantId = UUID.fromString(tenantIdString);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid tenant ID in token.");
        }

        if (email == null || email.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email not found in token.");
        }

        Student customer = studentRepository.findByTenantIdAndEmail(tenantId, email);

        if (customer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found.");
        }

        return customer;
    }

    public Student getOptionalAuthenticatedStudent(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        try {
            String token = authHeader.substring(7);

            if (jwtUtil.isTokenExpired(token)) {
                return null;
            }

            String email = jwtUtil.extractEmail(token);
            String tenantIdString = jwtUtil.extractTenantId(token);

            if (email == null || email.isEmpty() || tenantIdString == null) {
                return null;
            }

            UUID tenantId;
            try {
                tenantId = UUID.fromString(tenantIdString);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid tenant ID in token.");
            }

            return studentRepository.findByTenantIdAndEmail(tenantId, email);
        } catch (Exception e) {
            return null;
        }
    }

    public UUID validateAndGetUser(String subdomain) {
        if (subdomain == null || subdomain.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing subdomain header");
        }
        User user = adminRepository.findBySubDomain(subdomain);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid subdomain or tenant not found");
        }
        return user.getId();
    }


}
