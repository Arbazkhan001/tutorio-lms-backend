package com.affycloud.TutorioBackend.models;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

    @Data
    @Entity
    @Table(name = "orders")
    public class Order {

        @Id
        @GeneratedValue
        private UUID id;

        @ManyToOne
        @JoinColumn(name = "student_id", nullable = false)
        private Student student;

        @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<OrderItem> orderItems;

        @Column(nullable = false)
        private String status = "PENDING"; // Default status

        private String razorPayId;

        private String razorPayPaymentId;

        private String paymentStatus;

        @Column(nullable = false)
        private UUID tenantId;  // 🔥 REQUIRED

        @ManyToOne
        @JoinColumn(name = "coupon_id")
        private Coupon coupon; // nullable, only present if coupon applied

        private Double discountAmount;

        @Column(nullable = false)
        private Double amount;

        @Column(nullable = false)
        private Double totalAmount;

        @Column
        @CreationTimestamp
        private Date createdAt;


        // New field: randomId is generated inline using a static helper method
        @Column(unique = true, updatable = false, nullable = false)
        private String randomId = generateRandomId();

        private static final String PREFIX = "Order";
        private static final int RANDOM_PART_LENGTH = 5;
        private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        private static final Random RANDOM = new Random();

        private static String generateRandomId() {
            StringBuilder sb = new StringBuilder(PREFIX);
            for (int i = 0; i < RANDOM_PART_LENGTH; i++) {
                sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
            }
            return sb.toString();
        }
    }

