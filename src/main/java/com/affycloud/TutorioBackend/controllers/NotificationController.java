//package com.affycloud.TutorioBackend.controllers;
//
//import com.affycloud.TutorioBackend.dtos.notification.NotificationDetailDto;
//import com.affycloud.TutorioBackend.dtos.reponse.PaginatedResponse;
//import com.affycloud.TutorioBackend.services.NotificationService;
//import io.swagger.v3.oas.annotations.Operation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/notification")
//public class NotificationController {
//
//    @Autowired
//    private NotificationService notificationService;
//
//
//    @Operation(summary = "Get all notifications for admin")
//    @GetMapping("/admin")
//    public ResponseEntity<PaginatedResponse<NotificationDetailDto>> getAdminNotifications(
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(required = false) String status // "read", "unread", or null
//    ) {
//        return ResponseEntity.ok(notificationService.getAdminNotifications(page - 1, size, status));
//    }
//
//    @Operation(summary = "Get all notifications for a student")
//    @GetMapping("/student/{studentId}")
//    public ResponseEntity<PaginatedResponse<NotificationDetailDto>> getStudentNotifications(
//            @PathVariable UUID studentId,
//            @RequestParam(defaultValue = "1") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(required = false) String status // "past", "new", or null
//    ) {
//        return ResponseEntity.ok(notificationService.getStudentNotifications(studentId, page - 1, size, status));
//    }
//
//    @Operation(summary = "read notification by notificationId")
//    @PatchMapping("/{notificationId}/mark-as-read")
//    public ResponseEntity<Map<String, Object>> markNotificationAsRead(@PathVariable UUID notificationId) {
//        notificationService.markAsRead(notificationId);
//        Map<String, Object> response = new HashMap<>();
//        response.put("error", false);
//        response.put("message", "Notification marked as read successfully");
//        return ResponseEntity.ok(response);
//    }
//
//    @Operation(summary = "mark all notifications as read for admin")
//    @PatchMapping("/admin/mark-all-as-read")
//    public ResponseEntity<Map<String, Object>> markAllAdminNotificationsAsRead() {
//        notificationService.markAllAdminAsRead();
//        Map<String, Object> response = new HashMap<>();
//        response.put("error", false);
//        response.put("message", "All admin notifications marked as read successfully");
//        return ResponseEntity.ok(response);
//    }
//
//    @Operation(summary = "mark all notifications as read for student")
//    @PatchMapping("/student/{studentId}/mark-all-as-read")
//    public ResponseEntity<Map<String, Object>> markAllStudentNotificationsAsRead(@PathVariable UUID studentId) {
//        notificationService.markAllStudentAsRead(studentId);
//        Map<String, Object> response = new HashMap<>();
//        response.put("error", false);
//        response.put("message", "All student notifications marked as read successfully");
//        return ResponseEntity.ok(response);
//    }
//
//    @Operation(summary = "Delete a notification by ID")
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteNotification(@PathVariable UUID id) {
//        notificationService.deleteNotification(id);
//        Map<String, Object> response = new HashMap<>();
//        response.put("error", false);
//        response.put("message", "Notification deleted successfully");
//        return ResponseEntity.ok(response);
//    }
//
//}