package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.models.Notification;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByRecipientRoleOrderByCreatedAtDesc(Role role, Pageable pg);
    Page<Notification> findByStudentIdAndRecipientRole(UUID studentId, Role role, Pageable pageable);


    Page<Notification> findByRecipientRole(Role role, Pageable pageable);

    Page<Notification> findByRecipientRoleAndIsReadTrue(Role role, Pageable pageable);

    Page<Notification> findByRecipientRoleAndIsReadFalse(Role role, Pageable pageable);

    Page<Notification> findByStudentIdAndRecipientRoleAndCreatedAtBefore(UUID studentId, Role role, Date date, Pageable pageable);

    Page<Notification> findByStudentIdAndRecipientRoleAndCreatedAtAfter(UUID studentId, Role role, Date date, Pageable pageable);



    List<Notification> findByRecipientRoleAndIsReadFalse(Role role);

    List<Notification> findByStudentIdAndRecipientRole(UUID studentId, Role role);


}