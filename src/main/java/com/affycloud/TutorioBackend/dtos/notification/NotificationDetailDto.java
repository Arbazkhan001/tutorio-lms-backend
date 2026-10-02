package com.affycloud.TutorioBackend.dtos.notification;

import com.affycloud.TutorioBackend.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationDetailDto {
    private UUID id;
    private UUID itemId;
    private NotificationType type;
    private String message;
    private String metadata;
    private Boolean isRead;
    private Date createdAt;
    private Date updatedAt;


}
