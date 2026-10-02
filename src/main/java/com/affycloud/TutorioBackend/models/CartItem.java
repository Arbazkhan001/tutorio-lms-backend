package com.affycloud.TutorioBackend.models;


import com.affycloud.TutorioBackend.enums.ItemType;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;


    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_attempt_id")
    private CourseAttempt courseAttempt;

    @ManyToOne
    @JoinColumn(name = "course_batch_id")
    private CourseBatch courseBatch;

    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    @ManyToOne
    @JoinColumn(name = "ebook_id") 
    private Ebook ebook;  

    @ManyToOne
    @JoinColumn(name = "test_series_id") 
    private TestSeries testSeries; 

    @ManyToOne
    @JoinColumn(name = "book_id") 
    private Book book;

    @ManyToOne
    @JoinColumn(name = "course_bundle_id")
    private CourseBundle courseBundle;

    @Column
    private Integer quantity = 1;

}

