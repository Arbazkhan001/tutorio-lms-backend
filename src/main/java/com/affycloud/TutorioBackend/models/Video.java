package com.affycloud.TutorioBackend.models;


import com.affycloud.TutorioBackend.enums.ItemType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
public class Video {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    private ItemType itemType = ItemType.VIDEO;

    // private double price;
    private UUID courseTypeId;

    @ManyToOne
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    private String url;

    private Double countLimit;

    @Column
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    // Getters and Setters (Lombok @Data will handle this)
}
