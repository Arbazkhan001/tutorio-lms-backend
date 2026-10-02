package com.affycloud.TutorioBackend.models;

import com.affycloud.TutorioBackend.enums.ItemType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", length = 50) // Adjust length as needed
    private ItemType itemType;

    /*
     * @ManyToOne
     * 
     * @JoinColumn(name = "ebook_id")
     * private Ebook ebook;
     * 
     * @ManyToOne
     * 
     * @JoinColumn(name = "test_series_id")
     * private TestSeries testSeries;
     * 
     * @ManyToOne
     * 
     * @JoinColumn(name = "book_id")
     * private Book book;
     * 
     * @ManyToOne
     * 
     * @JoinColumn(name = "course_attempt_id")
     * private CourseAttempt courseAttempt;
     * 
     * @ManyToOne
     * 
     * @JoinColumn(name = "course_bundle_id")
     * private CourseBundle courseBundle;
     */

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "title")
    private String title;

    @Column(name = "courseType")
    private String courseType;

    @Column(name = "courseAttempt")
    private Date courseAttempt;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "discount_type")
    private String discountType;

    @Column(name = "discount")
    private Double discount;

    @Column(name = "final_price", nullable = false)
    private Double finalPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "course_batch_id")
    private UUID courseBatchId;

    @Column()
    private Double totalPrice; // New field for storing total price

    @Column
    @CreationTimestamp
    private Date createdAt;
}
