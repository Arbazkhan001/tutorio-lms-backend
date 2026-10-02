package com.affycloud.TutorioBackend.dtos.notification;

import lombok.Data;

@Data
public class NotificationRequestDto {
    private String token; // FCM device token
    private String title;
    private String body;
}
