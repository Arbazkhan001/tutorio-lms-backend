package com.affycloud.TutorioBackend.dtos.billingDetails;


import com.affycloud.TutorioBackend.dtos.address.AddressResponseDTO;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class BillingDetailDto {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String companyName;
    private String taxId;
    private AddressResponseDTO address;
    private Date createdAt;
    private Date updatedAt;
}
