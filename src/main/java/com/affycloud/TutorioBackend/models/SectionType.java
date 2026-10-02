package com.affycloud.TutorioBackend.models;



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
@Table(name = "section_type")
public class SectionType {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 50, unique = true, nullable = false)
    private String type; // HERO, MIDDLE, TOPPERS, BANNER etc.

    @OneToMany(mappedBy = "sectionType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    @Column
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;

}
