package com.affycloud.TutorioBackend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "doubt_conversations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"course_type_id", "student_id"}))
public class DoubtConversation {

    @Id
    @GeneratedValue
    private UUID id;

    // The course type for which the doubt conversation is created.
    @ManyToOne
    @JoinColumn(name = "course_type_id", nullable = false)
    @JsonIgnore
    private CourseType courseType;

    // The student who initiated the conversation.
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private Student student;

    // List of messages in this conversation.
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<DoubtMessage> messages = new ArrayList<>();

    @CreationTimestamp
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;
}
