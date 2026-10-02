package com.affycloud.TutorioBackend.dtos.billingDetails;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BillingDetailsUpdateDto {

    private String title;

    private String description;

    private String author;

    private MultipartFile thumbnail;
}
