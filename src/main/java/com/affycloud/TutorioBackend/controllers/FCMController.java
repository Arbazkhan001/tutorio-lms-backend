//package com.affycloud.TutorioBackend.controllers;
//
//import com.affycloud.TutorioBackend.dtos.notification.NotificationRequestDto;
//import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
//import com.affycloud.TutorioBackend.services.FCMService;
//import io.swagger.v3.oas.annotations.Operation;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/fcm")
//public class FCMController {
//
//    @Autowired
//    private FCMService fcmService;
//
//
//    @Operation(summary = "Send a notification to a specific device token")
//    @PostMapping("/send")
//    public ResponseEntity<ApiResponse> sendNotification(@RequestBody NotificationRequestDto request) {
//        String response = fcmService.sendNotificationToToken(request.getTitle(), request.getBody(), request.getToken());
//        return ResponseEntity.ok(
//                new ApiResponse("Notification sent successfully", false)
//        );
//    }
//}
//
