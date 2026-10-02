package com.affycloud.TutorioBackend.dtos.billingDetails;

import com.affycloud.TutorioBackend.dtos.address.AddressRequestDTO;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;


@Data
public class BillingDetailsAddDto {

    private String name;
    private String email;
    private String phone;

    private AddressRequestDTO address;

    private String companyName;
    private String taxId;   // e.g. VAT or GST

}
