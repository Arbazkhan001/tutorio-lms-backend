package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.banner.BannerUpsertDto;
import com.affycloud.TutorioBackend.models.Banner;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.services.BannerService;
import com.affycloud.TutorioBackend.utils.AuthUtilAdmin;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @Autowired
    private AuthUtilAdmin authUtil;

    @Operation(summary = "Create or update the admin's banner (upsert — one banner per admin)")
    @PostMapping
    public ResponseEntity<Map<String, Object>> upsertBanner(
            @RequestBody BannerUpsertDto request,
            HttpServletRequest httpRequest) {

        User admin = authUtil.getAuthenticatedUser(httpRequest);

        Map<String, Object> response = bannerService.upsertBanner(request, admin.getId());
        if ((boolean) response.getOrDefault("error", true)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get the admin's banner as-is, regardless of isActive/expiry")
    @GetMapping("/admin")
    public ResponseEntity<Map<String, Object>> getBannerForAdmin(HttpServletRequest httpRequest) {

        User admin = authUtil.getAuthenticatedUser(httpRequest);

        Banner banner = bannerService.getBannerForAdmin(admin.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("meta", banner);
        response.put("message", banner != null ? "Banner retrieved successfully" : "No banner found");
        response.put("error", false);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get the banner for students — only returned if active and within the showDays window")
    @GetMapping("/student")
    public ResponseEntity<Map<String, Object>> getBannerForStudent(
            @RequestHeader(value = "subdomain", required = true) String subdomain) {

        UUID tenantId = authUtil.validateAndGetUser(subdomain);

        Banner banner = bannerService.getVisibleBannerForStudent(tenantId);

        Map<String, Object> response = new HashMap<>();
        response.put("meta", banner);
        response.put("message", banner != null ? "Banner retrieved successfully" : "No active banner");
        response.put("error", false);
        return ResponseEntity.ok(response);
    }

}
