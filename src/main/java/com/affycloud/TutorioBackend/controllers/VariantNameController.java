package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.variants.VariantNameAddDto;
import com.affycloud.TutorioBackend.dtos.variants.VariantNameDetailDto;
import com.affycloud.TutorioBackend.dtos.variants.VariantNameDto;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.repositories.TeacherRepository;
import com.affycloud.TutorioBackend.services.VariantNameService;
import com.affycloud.TutorioBackend.utils.AuthUtilAdmin;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/variant-names")
public class VariantNameController {

    @Autowired
    private VariantNameService variantNameService;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private AuthUtilAdmin authUtil;

    @Operation(summary = "Create a new Variant-Name ")
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createVariantName(@RequestBody VariantNameDto variantNameDto) {
        Map<String, Object> response = variantNameService.createVariantName(variantNameDto);
        /* Storing value from the response. */
        Object value = response.get("response");
        // If there is no error, return 200 OK with the response
        if (response.containsKey("error") && !(boolean) response.get("error")) {
            return ResponseEntity.ok().body(response);
        }
        // If there is an error, return 400 BAD_REQUEST
        else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(summary = "Get All Variant-Names ")
    @GetMapping("/getAll")
    public ResponseEntity<?> getAllVariantNames() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<VariantNameDetailDto> categories = variantNameService.getAllVariantNames();
            response.put("meta", categories);  // "meta" contains the admin details
            response.put("message", "Variant-Names fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

    }

    // New API endpoint to get VariantName by variantType
    @Operation(summary = "Get Variant-Names by Variant-Type ")
    @GetMapping("/getByVariantType/{variantType}")
    public ResponseEntity<?> getVariantNameByVariantType(@PathVariable String variantType,
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
            VariantNameDetailDto variantNameDetail = variantNameService.getVariantNameByType(variantType,id);
            response.put("meta", variantNameDetail);
            response.put("message", "VariantName details fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            response.put("message", ex.getMessage());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // Add variantNames to an existing VariantName by variantType
    @Operation(summary = "Add Variant-Names in a Variant-Type ")
    @PostMapping("/addVariantNames/{variantType}")
    public ResponseEntity<Map<String, Object>> addVariantNames(
            @PathVariable String variantType,
            @RequestBody VariantNameAddDto variantNameAddDto,
            HttpServletRequest httpRequest) {
        User user = authUtil.getAuthenticatedUser(httpRequest);

        // Extract the list of variant names from the DTO
        List<String> newVariantNames = variantNameAddDto.getVariantNames();
        // Call the service to add these names
        Map<String, Object> response = variantNameService.addVariantNamesToExistingType(variantType, newVariantNames,user.getId());
        // Return appropriate response
        if (response.containsKey("error") && (boolean) response.get("error")) {
            // If error exists, check if it's a conflict (duplicate variant names)
            if (response.get("message").toString().contains("already exist")) {
                // Return 409 Conflict in case of duplicates
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            // Otherwise return 400 Bad Request for other errors
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            // If everything is fine, return 200 OK with the response
            return ResponseEntity.ok().body(response);
        }
    }

    // Remove variantNames from an existing VariantName by variantType
    @Operation(summary = "Remove Variant-Names in a Variant-Type ")
    @PostMapping("/removeVariantNames/{variantType}")
    public ResponseEntity<Map<String, Object>> removeVariantNames(
            @PathVariable String variantType, @RequestBody VariantNameAddDto variantNameAddDto,
            HttpServletRequest httpRequest) {
        User user = authUtil.getAuthenticatedUser(httpRequest);
        // Extract the list of variant names to remove from the DTO
        List<String> variantNamesToRemove = variantNameAddDto.getVariantNames();
        // Call the service to remove these names
        Map<String, Object> response = variantNameService.removeVariantNamesFromExistingType(variantType, variantNamesToRemove,user.getId());
        // Return appropriate response
        if (response.containsKey("error") && (boolean) response.get("error")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            return ResponseEntity.ok().body(response);
        }
    }

}
