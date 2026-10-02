package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.notification.NotificationDetailDto;
import com.affycloud.TutorioBackend.dtos.reponse.PaginatedResponse;
import com.affycloud.TutorioBackend.enums.NotificationType;
import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.handler.ResourceNotFoundException;
import com.affycloud.TutorioBackend.models.Notification;
import com.affycloud.TutorioBackend.models.Student;
import com.affycloud.TutorioBackend.repositories.NotificationRepository;
import com.affycloud.TutorioBackend.services.NotificationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class NotificationServiceImplementation implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private FCMServiceImplementation fcmService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Create & persist a Notification, then broadcast it over WebSocket.
     *
     * @param itemId    the domain ID (orderId, conversationId, submissionId…)
     * @param type      enum identifying the notification category
     * @param student   the student involved (may be null if not student‐specific)
     * @param message   human‐readable text
     * @param metadata  optional JSON blob for extra data
     * @return the saved Notification entity
     */
    public Notification createNotification(UUID itemId,
                                           NotificationType type,
                                           Student student,
                                           Role recipientRole,
                                           String message,
                                           String metadata) {
        Notification n = new Notification();
        n.setItemId(itemId);
        n.setType(type);
        n.setStudent(student);
        n.setRecipientRole(recipientRole);
        n.setMessage(message);
        n.setMetadata(metadata);
        Notification saved = notificationRepository.save(n);

        // broadcast to admin or student topic
        String topic = (recipientRole == Role.ADMIN
                ? "/topic/admin/notifications"
                : "/topic/student/" + student.getId() + "/notifications");
        messagingTemplate.convertAndSend(topic, saved);

        // 🔔 Send Firebase push notification (if student has token)
        try {
            String deviceToken = null;
            String title = "New Notification";
            if (recipientRole == Role.STUDENT && student != null && student.getFireBaseToken() != null) {
                deviceToken = student.getFireBaseToken();
            }

            if (deviceToken != null) {
                fcmService.sendNotificationToToken(title, message, deviceToken);
            }
        } catch (Exception e) {
            // Log and continue
            System.err.println("FCM push notification failed: " + e.getMessage());
        }
        return saved;
    }

    public PaginatedResponse<NotificationDetailDto> getAdminNotifications(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifications;

        if ("read".equalsIgnoreCase(status)) {
            notifications = notificationRepository.findByRecipientRoleAndIsReadTrue(Role.ADMIN, pageable);
        } else if ("unread".equalsIgnoreCase(status)) {
            notifications = notificationRepository.findByRecipientRoleAndIsReadFalse(Role.ADMIN, pageable);
        } else {
            notifications = notificationRepository.findByRecipientRole(Role.ADMIN, pageable);
        }

        List<NotificationDetailDto> dtoList = notifications.stream().map(this::mapToDto).toList();

        return PaginatedResponse.<NotificationDetailDto>builder()
                .totalPages(notifications.getTotalPages())
                .totalItems(notifications.getTotalElements())
                .size(notifications.getSize())
                .currentPage(notifications.getNumber())
                .meta(dtoList)
                .build();
    }

    public PaginatedResponse<NotificationDetailDto> getStudentNotifications(UUID studentId, int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> notifications;

        Date sevenDaysAgo = new Date(System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)); // 7 days in milliseconds

        if ("past".equalsIgnoreCase(status)) {
            notifications = notificationRepository.findByStudentIdAndRecipientRoleAndCreatedAtBefore(
                    studentId, Role.STUDENT, sevenDaysAgo, pageable);
        } else if ("new".equalsIgnoreCase(status)) {
            notifications = notificationRepository.findByStudentIdAndRecipientRoleAndCreatedAtAfter(
                    studentId, Role.STUDENT, sevenDaysAgo, pageable);
        } else {
            notifications = notificationRepository.findByStudentIdAndRecipientRole(
                    studentId, Role.STUDENT, pageable); // Let Sort.by(...).descending() handle ordering

        }

        List<NotificationDetailDto> dtoList = notifications.stream().map(this::mapToDto).toList();

        return PaginatedResponse.<NotificationDetailDto>builder()
                .totalPages(notifications.getTotalPages())
                .totalItems(notifications.getTotalElements())
                .size(notifications.getSize())
                .currentPage(notifications.getNumber())
                .meta(dtoList)
                .build();
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("Notification not found with ID: " + notificationId));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAdminAsRead() {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientRoleAndIsReadFalse(Role.ADMIN);
        unreadNotifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }


    @Transactional
    public void markAllStudentAsRead(UUID studentId) {
        List<Notification> notifications = notificationRepository.findByStudentIdAndRecipientRole(studentId, Role.STUDENT);
        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(notifications);
    }




    private NotificationDetailDto mapToDto(Notification notification) {
        return NotificationDetailDto.builder()
                .id(notification.getId())
                .itemId(notification.getItemId())
                .type(notification.getType())
                .message(notification.getMessage())
                .metadata(notification.getMetadata())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }

    @Transactional
    public void deleteNotification(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with ID: " + notificationId));
        notificationRepository.delete(notification);
    }


}