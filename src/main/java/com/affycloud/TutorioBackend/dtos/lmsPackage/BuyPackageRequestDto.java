package com.affycloud.TutorioBackend.dtos.lmsPackage;


import com.affycloud.TutorioBackend.dtos.billingDetails.BillingDetailsAddDto;
import com.affycloud.TutorioBackend.dtos.billingDetails.CardAddDto;
import com.affycloud.TutorioBackend.dtos.billingDetails.CardDetailsDto;
import lombok.Data;

import java.util.UUID;

@Data
public class BuyPackageRequestDto {
    private UUID packageId;
    private BillingDetailsAddDto billingDetails;
    private CardAddDto cardDetails;
}