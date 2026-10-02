package com.affycloud.TutorioBackend.models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.*;

@Data
@Entity
@Table(name = "lms_packages")
public class LmsPackage {
    @Id
    @GeneratedValue
    private UUID id;

    private String packageName;
    private String packageDescription;
    private Double price;
    private String discountType;
    private Double discountValue;
    private Double finalPrice;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "lms_package_features",
            joinColumns = @JoinColumn(name = "lms_package_id"),
            inverseJoinColumns = @JoinColumn(name = "feature_id")
    )
    private List<Feature> features = new ArrayList<>();

    @Column
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;

}


