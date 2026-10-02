package com.affycloud.TutorioBackend.models;

import com.affycloud.TutorioBackend.enums.ItemType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Entity
public class Book {

    @Id
    @GeneratedValue
    private UUID bookId;

    private String title;

    @Column(length = 10000)
    private String description;

    private String author;
    private Double price;
    private String discountType;
    private Double discount;
    private Double finalPrice = 0.0;

    /* This fields for image path */
    // This field stores multiple image paths as a comma-separated string
    @ElementCollection
    @CollectionTable(name = "images", joinColumns = @JoinColumn(name = "book_id"))
    @Column(name = "image_path")
    private List<String> images = new ArrayList<>();

    private String released;

    @Enumerated(EnumType.STRING)
    private ItemType itemType = ItemType.BOOK;

    private String language;

    private Boolean isActive = true;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "created_by_id", nullable = false)
    private UUID createdById;

    @Column
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;

}
