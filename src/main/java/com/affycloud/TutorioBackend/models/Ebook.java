package com.affycloud.TutorioBackend.models;

import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.enums.ItemType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
public class Ebook {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;

    @Column(length = 10000)
    private String description;

    private Double price=0.0;
    private String discountType="none";
    private Double discount;
    private Double finalPrice = 0.0;

    @Enumerated(EnumType.STRING)
    private ItemType itemType = ItemType.EBOOK;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType = PaymentType.FREE;

    private UUID courseTypeId;

    private String url;
    private String thumbnailUrl;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "created_by_id", nullable = false)
    private UUID createdById;

    @Column
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

}