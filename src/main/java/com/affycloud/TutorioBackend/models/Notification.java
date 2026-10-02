package com.affycloud.TutorioBackend.models;

import com.affycloud.TutorioBackend.enums.NotificationType;
import com.affycloud.TutorioBackend.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue
    private UUID id;                                             // notificationId

    @Column(nullable = false)
    private UUID itemId;                                         // orderId, doubtId, submissionId, etc.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;                              // ORDER, DOUBT, SUBMISSION, etc.

    @ManyToOne(optional = true)                                           // allow null
    @JoinColumn(name = "student_id", nullable = true)                     // column can be null :contentReference[oaicite:0]{index=0}
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role recipientRole;     // e.g. ADMIN or STUDENT :contentReference[oaicite:1]{index=1}


    @Column(length = 500)
    private String message;                                     // human-readable message

    @Column(columnDefinition = "TEXT")
    private String metadata;                                    // JSON blob for extra info

    @Column(nullable = false)
    private Boolean isRead = false;                               // read/unread flag

    @Column
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;
}

