package com.affycloud.TutorioBackend.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "features")
public class Feature {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String featureKey;   // auto-generated, unique

    private String description;

    @JsonIgnore
    @ManyToMany(mappedBy = "features")
    private Set<LmsPackage> lmsPackages = new HashSet<>();

    @Column
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;

}


