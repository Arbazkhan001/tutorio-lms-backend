package com.affycloud.TutorioBackend.dtos.Cart;

import lombok.Data;

import java.util.List;
import java.util.UUID;


@Data
public class CartDetailsDto {

    private UUID studentId;
    private List<CartItemDto> cartItems;

    // getters and setters
    @Data
    public static class CartItemDto {
        private UUID id;
        private Integer attempt;
        private Double price;
        private String discountType;
        private Double discount;
        private Double finalPrice;
        private CourseTypeDto courseType;
        private BookDto book;
        private CourseBundleDto courseBundleDto;

        // getters and setters
    }
    @Data
    public static class CourseTypeDto {
        private UUID id;
        private String description;
        private String courseType;
        private CourseDto course;

        // getters and setters
    }
    @Data
    public static class CourseDto {
        private UUID id;
        private String title;
        private String category;
        private String subject;
        private String thumbnail;
        private boolean isActive;
        private boolean isPopular;
        private boolean onBanner;

        // getters and setters
    }
    @Data
    public static class BookDto {
        private UUID id;
        private String title;
        private String author;
        private String isbn;
        private String thumbnail;
        private boolean isActive;

        // getters and setters
    }

    @Data
    public static class CourseBundleDto {
        private UUID id;
        private String title;
        private String description;
        private String thumbnail;
        // getters and setters
    }
}
