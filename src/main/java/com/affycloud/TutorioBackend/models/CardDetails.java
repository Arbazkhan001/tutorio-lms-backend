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
@Table(name = "card_details")
public class CardDetails {
    @Id
    @GeneratedValue
    private UUID id;

    private String cardholderName;

    // Store full card number (later encrypt this field)
    @Column(length = 25, nullable = false)
    private String cardNumber;

    private Integer expiryMonth;
    private Integer expiryYear;

    private String cvv; // keep for now, but best practice is not to store CVV in production

    private String cardType;  // VISA/MASTERCARD

    @OneToOne(mappedBy = "cardDetails")
    private PackagePurchase purchase;

    @Column(updatable = false)
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;

}