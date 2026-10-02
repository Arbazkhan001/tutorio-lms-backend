//package com.affycloud.TutorioBackend.controllers;
//
//import com.affycloud.TutorioBackend.dtos.blog.BlogAddDto;
//import com.affycloud.TutorioBackend.dtos.blog.BlogDetailDto;
//import com.affycloud.TutorioBackend.dtos.blog.BlogUpdateDto;
//import com.affycloud.TutorioBackend.services.BlogService;
//import io.swagger.v3.oas.annotations.Operation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/blog")
//public class BlogController {
//
//    @Autowired
//    private BlogService service;
//
//    @Operation(summary = "Create a Blog")
//    @PostMapping("create")
//    public ResponseEntity<Map<String, Object>> createBlog(@ModelAttribute BlogAddDto request) throws IOException {
//        Map<String, Object> response = service.createBlog(request);
//        /* Storing value from the response. */
//        Object value = response.get("response");
//        // If there is no error, return 200 OK with the response
//        if (response.containsKey("error") && !(boolean) response.get("error")) {
//            return ResponseEntity.ok().body(response);
//        }
//        // If there is an error, return 400 BAD_REQUEST or 409 CONFLICT depending on the case
//        else {
//            String errorResponse = (String) value;
//            if (errorResponse.contains("already exists")) {
//                return ResponseEntity.status(HttpStatus.IM_USED).body(response);
//            } else {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//            }
//        }
//    }
//
//
//    @Operation(summary = "Get All Blogs ")
//    @GetMapping("/all")
//    public ResponseEntity<?> getAllBlogs() {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            List<BlogDetailDto> admins = service.getAllBlogs();
//            response.put("meta", admins);  // "meta" contains the admins details
//            response.put("message", "Blogs fetched successfully");
//            response.put("error", false);
//            return ResponseEntity.ok(response);
//        } catch (ResponseStatusException ex) {
//            response.put("message", ex.getReason());
//            response.put("error", true);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//    }
//
//    @Operation(summary = "Update a Blog")
//    @PutMapping("/{id}")
//    public ResponseEntity<?> updateBlog(@PathVariable UUID id, @ModelAttribute BlogUpdateDto request) throws IOException {
//        Map<String, Object> response = service.updateBlog(id, request);
//
//        if (response.containsKey("error") && !(boolean) response.get("error")) {
//            return ResponseEntity.ok(response);
//        } else {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
//        }
//    }
//
//
//
//    @Operation(summary = "Get All Active Blogs ")
//    @GetMapping("/active/all")
//    public ResponseEntity<?> getAllActiveBlogs() {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            List<BlogDetailDto> admins = service.getAllActiveBlogs();
//            response.put("meta", admins);  // "meta" contains the admins details
//            response.put("message", "Blogs fetched successfully");
//            response.put("error", false);
//            return ResponseEntity.ok(response);
//        } catch (ResponseStatusException ex) {
//            response.put("message", ex.getReason());
//            response.put("error", true);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//    }
//
//
//    @Operation(summary = "Get Blog by ID")
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getBlogById(@PathVariable UUID id) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            BlogDetailDto admin = service.getBlogById(id);
//            response.put("meta", admin);  // "meta" contains the course details
//            response.put("message", "Blog fetched successfully");
//            return ResponseEntity.ok(response);
//        } catch (ResponseStatusException ex) {
//            response.put("message", ex.getReason());
//            response.put("error", "true");
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//    }
//
//}
