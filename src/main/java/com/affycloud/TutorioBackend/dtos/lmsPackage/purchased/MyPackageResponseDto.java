package com.affycloud.TutorioBackend.dtos.lmsPackage.purchased;


import com.affycloud.TutorioBackend.dtos.billingDetails.BillingDetailDto;
import com.affycloud.TutorioBackend.dtos.billingDetails.BillingDetailsAddDto;
import com.affycloud.TutorioBackend.dtos.billingDetails.CardDetailsDto;
import com.affycloud.TutorioBackend.dtos.lmsPackage.PackageDetailDto;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class MyPackageResponseDto {
    private PackageDetailDto lmsPackage;

    private BillingDetailDto billingDetails;

    private CardDetailsDto cardDetails;

    private String transactionStatus;
    private String paymentReference;

    private Date purchasedAt;
}