package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.feature.FeatureAddDto;
import com.affycloud.TutorioBackend.dtos.feature.FeatureDetailDto;
import com.affycloud.TutorioBackend.dtos.feature.FeatureUpdateDto;
import com.affycloud.TutorioBackend.services.FeatureService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/feature")
public class FeatureController {

    @Autowired
    private FeatureService service;

    @Operation(summary = "Create a Feature")
    @PostMapping("create")
    public ResponseEntity<Map<String, Object>> createFeatures(@RequestBody FeatureAddDto request) {
        Map<String, Object> response = service.createFeatures(request);

        if (response.containsKey("error") && !(boolean) response.get("error")) {
            return ResponseEntity.ok().body(response);
        } else {
            String errorMessage = (String) response.get("message");
            if (errorMessage != null && errorMessage.contains("already exists")) {
                return ResponseEntity.status(HttpStatus.IM_USED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
    }



    @Operation(summary = "Get All Features ")
    @GetMapping("/all")
    public ResponseEntity<?> getAllFeatures() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<FeatureDetailDto> features = service.getAllFeatures();
            response.put("meta", features);  // "meta" contains the features details
            response.put("message", "Features fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Update a Feature")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFeature(@PathVariable UUID id, @RequestBody FeatureUpdateDto request) throws IOException {
        Map<String, Object> response = service.updateFeature(id, request);

        if (response.containsKey("error") && !(boolean) response.get("error")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @Operation(summary = "Get Feature by ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getFeatureById(@PathVariable UUID id) {
        Map<String, Object> response = new HashMap<>();
        try {
            FeatureDetailDto feature = service.getFeatureById(id);
            response.put("meta", feature);  // "meta" contains the feature details
            response.put("message", "Feature fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

}
