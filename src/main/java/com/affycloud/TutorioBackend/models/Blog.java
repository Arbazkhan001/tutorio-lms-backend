package com.affycloud.TutorioBackend.models;



import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "blogs")
public class Blog {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 128)
    private String title;

    @Column(length = 5000)
    private String description;

    @Column(length = 128)
    private String author;

    @Column(length = 128)
    private String slug;

    private String thumbnail;

    private Boolean isActive = true;

    @Column(updatable = false)
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;

}