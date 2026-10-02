package com.affycloud.TutorioBackend.dtos.ebook;

import com.affycloud.TutorioBackend.dtos.paymentType.PaymentType;
import com.affycloud.TutorioBackend.enums.ItemType;
import lombok.Data;

import java.util.Date;

@Data
public class EbookDetailDto {


    private String id;
    private String title;
    private String description;
    private PaymentType paymentType;
    private Double price;
    private String discountType;
    private Double discount;
    private Double finalPrice;
    private ItemType itemType;
    private String url;
    private String thumbnailUrl;
    private String courseTypeId;
    private boolean inMyLearning;
    private boolean inCart;
    private Date createdAt;


}
