package com.affycloud.TutorioBackend.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "meetings")
public class Meeting {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String meetingId;

    @Column(nullable = false)
    private String meetingUrl;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String startTime;

    @Column(nullable = false)
    private int duration;  // in minutes

    @Column(nullable = true)
    private String agenda;

    @ManyToOne
    @JoinColumn(name = "course_attempt_id", nullable = false)  
    private CourseAttempt courseAttempt;

     @Column(nullable = false)
    private boolean canceled = false; 

    @Column(nullable = false, length = 1000)
    private String startUrl;

}

