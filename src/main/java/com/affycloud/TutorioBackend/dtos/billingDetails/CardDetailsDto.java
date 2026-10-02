package com.affycloud.TutorioBackend.dtos.billingDetails;


import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class CardDetailsDto {

    private UUID id;
    private String cardholderName;
    private String cardNumber;
    private Integer expiryMonth;
    private Integer expiryYear;
    private String cvv;
    private String cardType; // Optional (VISA/MASTERCARD etc.)
    private Date createdAt;
    private Date updatedAt;
}

