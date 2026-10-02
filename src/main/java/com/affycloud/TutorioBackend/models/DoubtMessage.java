package com.affycloud.TutorioBackend.models;

import com.affycloud.TutorioBackend.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "doubt_messages")
public class DoubtMessage {

    @Id
    @GeneratedValue
    private UUID id;

    // Link to the parent conversation.
    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    @JsonIgnore
    private DoubtConversation conversation;

    // Indicates whether the sender is a STUDENT or a USER (TEACHER/ADMIN)
    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false)
    private Role senderRole;

    // If senderRole == STUDENT, this field is populated.
    @ManyToOne(optional = true)
    @JoinColumn(name = "student_sender_id")
    private Student studentSender;

    // If senderRole == TEACHER or ADMIN, this field is populated.
    @ManyToOne(optional = true)
    @JoinColumn(name = "user_sender_id")
    private User userSender;

    // The message content.
    @Column(length = 4000)
    private String messageText;

    @CreationTimestamp
    private Date createdAt;
}

