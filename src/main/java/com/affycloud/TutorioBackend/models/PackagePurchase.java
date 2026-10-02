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
@Table(name = "package_purchases")
public class PackagePurchase {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "package_id")
    private LmsPackage lmsPackage;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "billing_id")
    private BillingDetails billingDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "card_id")
    private CardDetails cardDetails;

    private String transactionStatus; // SUCCESS, FAILED, PENDING
    private String paymentReference;  // e.g. Razorpay/Stripe txn id

    @Column(updatable = false)
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;
}
