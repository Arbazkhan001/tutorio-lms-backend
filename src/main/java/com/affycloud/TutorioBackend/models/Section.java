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
public class Section {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 128)
    private String title;

    @Column(length = 3000)
    private String description;

    private String thumbnail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_type_id", nullable = false)
    private SectionType sectionType;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "created_by_id", nullable = false)
    private UUID createdById;

/*    @Column(length = 15)
    private String sectionType;*/

    @Column
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;

}
