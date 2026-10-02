package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.feature.FeatureAddDto;
import com.affycloud.TutorioBackend.dtos.feature.FeatureDetailDto;
import com.affycloud.TutorioBackend.dtos.feature.FeatureUpdateDto;
import com.affycloud.TutorioBackend.dtos.lmsPackage.BuyPackageRequestDto;
import com.affycloud.TutorioBackend.dtos.lmsPackage.PackageAddDto;
import com.affycloud.TutorioBackend.dtos.lmsPackage.PackageDetailDto;
import com.affycloud.TutorioBackend.dtos.lmsPackage.PackageUpdateDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.services.FeatureService;
import com.affycloud.TutorioBackend.services.PackageService;
import com.affycloud.TutorioBackend.utils.AuthUtilAdmin;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/package")
public class PackageController {


    @Autowired
    private PackageService service;


    @Autowired
    private AuthUtilAdmin authUtil;

    @Operation(summary = "Create a Package")
    @PostMapping("/create")
    public ResponseEntity<?> createPackage(@RequestBody PackageAddDto request) {
        Map<String, Object> response = service.createPackage(request);
        return ResponseEntity.status((boolean) response.get("error") ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Operation(summary = "Update a Package")
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePackage(@PathVariable UUID id, @RequestBody PackageUpdateDto request) {
        Map<String, Object> response = service.updatePackage(id, request);
        return ResponseEntity.status((boolean) response.get("error") ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @Operation(summary = "Get Package by ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getPackageById(@PathVariable UUID id) {
        PackageDetailDto dto = service.getPackageById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("meta", dto);
        response.put("message", "Package fetched successfully.");
        response.put("error", false);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get All Packages ")
    @GetMapping("/all")
    public ResponseEntity<?> getAllPackages() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<PackageDetailDto> features = service.getAllPackages();
            response.put("meta", features);  // "meta" contains the features details
            response.put("message", "Packages fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Buy a package (only ADMIN can buy)")
    @PostMapping("/buy")
    public ResponseEntity<ApiResponse> buyPackage(
            @RequestBody BuyPackageRequestDto request,
            HttpServletRequest httpRequest) {

        User user = authUtil.getAuthenticatedUser(httpRequest);
        ApiResponse response = service.buyPackage(user.getId(), request);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.CONFLICT : HttpStatus.OK)
                .body(response);
    }

    @Operation(summary = "Get my purchased package details with billing (only ADMIN)")
    @GetMapping("/billing/my-package")
    public ResponseEntity<ApiResponseWithMeta> getMyPackageWithBilling(HttpServletRequest httpRequest) {
        User user = authUtil.getAuthenticatedUser(httpRequest);
        ApiResponseWithMeta response = service.getMyPackageWithBilling(user.getId());

        return ResponseEntity
                .status(response.isError() ? HttpStatus.CONFLICT : HttpStatus.OK)
                .body(response);
    }


    @Operation(summary = "Get my purchased package (only ADMIN)")
    @GetMapping("/my-package")
    public ResponseEntity<ApiResponseWithMeta> getMyPackage(HttpServletRequest httpRequest) {
        User user = authUtil.getAuthenticatedUser(httpRequest);
        ApiResponseWithMeta response = service.getMyPackage(user.getId());

        return ResponseEntity
                .status(response.isError() ? HttpStatus.CONFLICT : HttpStatus.OK)
                .body(response);
    }

    @Operation(summary = "Get All Features with package mapping")
    @GetMapping("/all/features/{packageId}")
    public ResponseEntity<?> getAllFeaturesWithPackage(@PathVariable UUID packageId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<FeatureDetailDto> features = service.getAllFeaturesWithPackage(packageId);
            response.put("meta", features);
            response.put("message", "Features fetched successfully.");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Toggle Feature in Package (Add if not present, Remove if already present)")
    @PatchMapping("/toggle-feature/{packageId}/{featureId}")
    public ResponseEntity<?> toggleFeatureInPackage(@PathVariable UUID packageId, @PathVariable UUID featureId) {
        Map<String, Object> response = new HashMap<>();
        try {
            String message = service.toggleFeatureInPackage(packageId, featureId);
//            response.put("meta", updatedPackage);
            response.put("message", message);
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }


}
