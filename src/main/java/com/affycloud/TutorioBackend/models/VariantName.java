package com.affycloud.TutorioBackend.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "variant_name")
public class VariantName {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 512)
    private String variantType; // Example: Collection, Category, Subject etc.

    @OneToMany(mappedBy = "variantName", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VariantValue> variantValues = new ArrayList<>();

    /*@ElementCollection
    @CollectionTable(name = "variant_name_names", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "names")
    private List<String> variantNames = new ArrayList<>();*/



}
