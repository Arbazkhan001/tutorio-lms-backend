//package com.affycloud.TutorioBackend.controllers;
//
//
//import com.affycloud.TutorioBackend.dtos.coupon.CouponRequestDTO;
//import com.affycloud.TutorioBackend.dtos.coupon.CouponResponseDTO;
//import com.affycloud.TutorioBackend.services.CouponService;
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
//@RequestMapping("/api/coupons")
//public class CouponController {
//
//    @Autowired
//    private CouponService service;
//
//    @Operation(summary = "Create a Blog")
//    @PostMapping("create")
//    public ResponseEntity<Map<String, Object>> createBlog(@RequestBody CouponRequestDTO request) throws IOException {
//        Map<String, Object> response = service.createCouponForStudent(request);
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
//    @Operation(summary = "Get All Coupons ")
//    @GetMapping("/all")
//    public ResponseEntity<?> getAllCoupons() {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            List<CouponResponseDTO> admins = service.getAllCoupons();
//            response.put("meta", admins);  // "meta" contains the admins details
//            response.put("message", "Coupons fetched successfully");
//            response.put("error", false);
//            return ResponseEntity.ok(response);
//        } catch (ResponseStatusException ex) {
//            response.put("message", ex.getReason());
//            response.put("error", true);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//    }
//
//    @Operation(summary = "Get All Student Coupons ")
//    @GetMapping("/{studentId}/all")
//    public ResponseEntity<?> getStudentsAllCoupons(@PathVariable UUID studentId) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            List<CouponResponseDTO> admins = service.getStudentsAllCoupons(studentId);
//            response.put("meta", admins);  // "meta" contains the admins details
//            response.put("message", "Coupons fetched successfully");
//            response.put("error", false);
//            return ResponseEntity.ok(response);
//        } catch (ResponseStatusException ex) {
//            response.put("message", ex.getReason());
//            response.put("error", true);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//    }
//
//    @Operation(summary = "Get Coupon by ID")
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getCouponById(@PathVariable UUID id) {
//        Map<String, Object> response = new HashMap<>();
//        try {
//            CouponResponseDTO coupon = service.getCouponById(id);
//            response.put("meta", coupon);  // "meta" contains the coupon details
//            response.put("message", "Coupons fetched successfully");
//            response.put("error", false);
//            return ResponseEntity.ok(response);
//        } catch (ResponseStatusException ex) {
//            response.put("message", ex.getReason());
//            response.put("error", true);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//        }
//    }
//}
