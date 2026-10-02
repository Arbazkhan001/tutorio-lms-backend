package com.affycloud.TutorioBackend.models;

import com.affycloud.TutorioBackend.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
//    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(length = 128)
    private String name;

    @Column(length = 512)
    private String email;


    @Column(length = 350)
    private String orgarnisationName;

    @Column(length = 350)
    private String subDomain;

    @Column(length = 128)
    private String orgarnisationType;

    @Column(length = 15)
    private String phoneNumber;

    @Column(length = 256)
    private String username;

    @Column(length = 70)
    private String password;

    // Role should be an enum for better type safety
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Role role;

    @Column(length = 50)
    private boolean isDeleted = false;  // Wrapper class, can accept null

    private Boolean isActive = true;

    @OneToOne
    @JoinColumn(name = "package_id")
    private LmsPackage lmsPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")  // Reference to the parent user (ADMIN -> TEACHER)
    private User parent;  // TEACHER, ADMIN, etc. (hierarchical relation)

    @Column(name = "razorpay_key")
    private String razorpayKey;

    @Column(name = "razorpay_secret")
    private String razorpaySecret;

    @Column
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;

    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;

}
