package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.section.SectionAddDto;
import com.affycloud.TutorioBackend.dtos.section.SectionDetailDto;
import com.affycloud.TutorioBackend.dtos.section.SectionUpdateDto;
import com.affycloud.TutorioBackend.models.User;
import com.affycloud.TutorioBackend.repositories.TeacherRepository;
import com.affycloud.TutorioBackend.services.SectionService;
import com.affycloud.TutorioBackend.utils.AuthUtilAdmin;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/sections")
public class SectionController {

    @Autowired
    private SectionService service;

    @Autowired
    private AuthUtilAdmin authUtil;

    @Autowired
    private TeacherRepository teacherRepository;

//    @Operation(summary = "Create a section")
//    @PostMapping(value = "/create" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<Map<String, Object>> createOrUpdate(@ModelAttribute SectionAddDto request,
//                                                              @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
//                                                              HttpServletRequest httpRequest ) throws IOException {
//        User user = authUtil.getAuthenticatedUser(httpRequest);
//        Map<String, Object> response = service.createOrUpdate(request, thumbnail, user);
//        if ((boolean) response.get("error")) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        }
//
//        return ResponseEntity.ok(response);
//    }

    @Operation(summary = "Create section with file")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createOrUpdateMultipart(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("sectionType") String sectionType,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            HttpServletRequest httpRequest) throws IOException {

        SectionAddDto request = new SectionAddDto();
        request.setTitle(title);
        request.setDescription(description);
        request.setSectionType(sectionType);


        User user = authUtil.getAuthenticatedUser(httpRequest);
        Map<String, Object> response = service.createOrUpdate(request, thumbnail, user);

        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Get All Sections ")
    @GetMapping("/all")
    public ResponseEntity<?> getAllSections(HttpServletRequest httpRequest,
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
            List<SectionDetailDto> admins = service.getAllSections(id);
            response.put("meta", admins);  // "meta" contains the admins details
            response.put("message", "Sections fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", true);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }


    @Operation(summary = "Get Section by ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getSectionById(@PathVariable UUID id) {
        Map<String, Object> response = new HashMap<>();
        try {
            SectionDetailDto admin = service.getSectionById(id);
            response.put("meta", admin);  // "meta" contains the Section details
            response.put("message", "Section fetched successfully");
            response.put("error", "false");
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            response.put("message", ex.getReason());
            response.put("error", "true");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @Operation(summary = "Delete Section by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSectionById(@PathVariable UUID id) {
        Map<String, Object> response = service.deleteSectionById(id);
        if ((boolean) response.get("error")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Section by Type")
    @GetMapping("/type/{sectionType}")
    public ResponseEntity<Map<String, Object>> getSectionByType(@PathVariable String sectionType,
                                                                HttpServletRequest httpRequest,
                                                                @RequestHeader(value = "subdomain", required = true) String subdomain) {

        Map<String, Object> response = new HashMap<>();
        UUID tenantId = authUtil.validateAndGetUser(subdomain);

        response = service.getSectionByType(sectionType,tenantId);
        if ((boolean) response.get("error")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Section by ID")
    @PutMapping(value = "/{id}" , consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> updateSection(
            @PathVariable UUID id,
            @ModelAttribute SectionUpdateDto request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) throws IOException {

        Map<String, Object> response = service.updateSection(id, thumbnail,request);

        if ((boolean) response.get("error")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.ok(response);
    }

}