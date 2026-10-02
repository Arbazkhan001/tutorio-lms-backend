package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.course.subject.SubjectAddDto;
import com.affycloud.TutorioBackend.dtos.course.subject.SubjectDetailDto;
import com.affycloud.TutorioBackend.dtos.course.subject.chapter.ChapterAddDto;
import com.affycloud.TutorioBackend.dtos.course.subject.chapter.ChapterDetailDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.handler.ResourceNotFoundException;
import com.affycloud.TutorioBackend.models.Chapter;
import com.affycloud.TutorioBackend.models.Subject;
import com.affycloud.TutorioBackend.services.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class SubjectController {

    @Autowired
    private SubjectService service;

    @Operation(summary = " Create a new subject with chapters")
    @PostMapping("/subject/create")
    public ResponseEntity<Map<String, Object>> createSubjectWithChapters(
                      @RequestBody SubjectAddDto dto) {
        Subject subject = service.createSubjectWithChapters(dto);
        // Custom Response Formatting
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", false);
        response.put("message", "Subject created successful.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update subject name")
    @PutMapping("/subject/update/{id}")
    public ResponseEntity<ApiResponse> updateSubject( @PathVariable UUID id,
            @RequestParam String name) {

        service.updateSubjectName(name , id);

        ApiResponse response = new ApiResponse(
                "Subject updated successfully",
                false
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update chapter name")
    @PutMapping("/chapter/update/{id}")
    public ResponseEntity<ApiResponse> updateChapter( @PathVariable UUID id,
                                                      @RequestParam String name) {

        service.updateChapterName(name , id);

        ApiResponse response = new ApiResponse(
                "Chapter  updated successfully",
                false
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Toggle Subject Active Status")
    @PutMapping("/subject/toggle-active/{subjectId}")
    public ResponseEntity<?> toggleSubjectIsActive(@PathVariable UUID subjectId) {
        Map<String, Object> response = service.toggleSubjectIsActive(subjectId);
        if ((boolean) response.getOrDefault("error", true)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Toggle Chapter Active Status")
    @PutMapping("/chapter/toggle-active/{chapterId}")
    public ResponseEntity<?> toggleChapterIsActive(@PathVariable UUID chapterId) {
        Map<String, Object> response = service.toggleChapterIsActive(chapterId);
        if ((boolean) response.getOrDefault("error", true)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create a new chapter in a subject")
    @PostMapping("/chapter/create")
    public ResponseEntity<Map<String, Object>> createChapter(@RequestBody ChapterAddDto request) {
        try {
            // Call the service to create a chapter
            Chapter chapter = service.createChapter(request);
            // Prepare custom response
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("error", false);
            response.put("message", "Chapter created successfully.");
            response.put("chapter", chapter); // Optional: If you want to return the chapter object in the response
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException ex) {
            // Handling exception for subject not found
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("error", true);
            response.put("message", ex.getMessage()); // Return the exception message (e.g., "Subject not found")
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception ex) {
            // General exception handling
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("error", true);
            response.put("message", "An error occurred while creating the chapter.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "Get all subjects by course type ID")
    @GetMapping("/subject/by-course/{courseTypeId}")
    public ResponseEntity<Map<String, Object>> getSubjectsByCourseType(@PathVariable UUID courseTypeId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<SubjectDetailDto> subjects = service.getSubjectsByCourseType(courseTypeId);
            response.put("meta", subjects);
            response.put("message", "Subjects fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to fetch subjects");
            response.put("error", true);
            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "Get all chapters by subject ID")
    @GetMapping("/chapter/by-subject/{subjectId}")
    public ResponseEntity<Map<String, Object>> getChaptersBySubject(@PathVariable UUID subjectId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ChapterDetailDto> chapters = service.getChaptersBySubject(subjectId);
            response.put("meta", chapters);
            response.put("message", "Chapters fetched successfully");
            response.put("error", false);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("message", "Failed to fetch chapters");
            response.put("error", true);
            return ResponseEntity.status(500).body(response);
        }
    }

}
