package com.affycloud.TutorioBackend.models;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "banners")
public class Banner {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 256)
    private String title;

    @Column(nullable = false, length = 5000)
    private String description;

    private Boolean isActive = false;

    @Column(nullable = false)
    private Integer showDays;

    // One banner per admin
    @Column(name = "tenant_id", nullable = false, unique = true)
    private UUID tenantId;

    @Column(updatable = false)
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    // Set explicitly on every upsert so the visibility window resets even when no other field changed
    @Column
    private Date updatedAt;

}
