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
@Table(name = "billing_details")
public class BillingDetails {
    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String email;
    private String phone;

    private String companyName;
    private String taxId;   // GST / VAT

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "billing_address_id")
    private BillingAddress billingAddress;

    @OneToOne(mappedBy = "billingDetails")
    private PackagePurchase purchase;

    @Column(updatable = false)
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;

}