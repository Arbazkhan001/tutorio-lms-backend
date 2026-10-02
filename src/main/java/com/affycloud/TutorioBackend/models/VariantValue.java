package com.affycloud.TutorioBackend.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "variant_value")
public class VariantValue {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    private UUID tenantId; // har admin ka alag tenant

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_name_id")
    private VariantName variantName;

}
