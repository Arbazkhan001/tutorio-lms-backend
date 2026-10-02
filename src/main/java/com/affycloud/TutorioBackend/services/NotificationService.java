package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.notification.NotificationDetailDto;
import com.affycloud.TutorioBackend.dtos.reponse.PaginatedResponse;
import com.affycloud.TutorioBackend.enums.NotificationType;
import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.models.Notification;
import com.affycloud.TutorioBackend.models.Student;

import java.util.UUID;

public interface NotificationService {

    Notification createNotification(UUID itemId,
                                    NotificationType type,
                                    Student student,
                                    Role recipientRole,
                                    String message,
                                    String metadata);
    PaginatedResponse<NotificationDetailDto> getAdminNotifications(int page, int size, String status);
    PaginatedResponse<NotificationDetailDto> getStudentNotifications(UUID studentId, int page, int size, String status);
    void markAsRead(UUID notificationId);
    void markAllAdminAsRead();
    void markAllStudentAsRead(UUID studentId);
    void deleteNotification(UUID notificationId);

}
