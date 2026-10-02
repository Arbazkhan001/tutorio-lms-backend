package com.affycloud.TutorioBackend.models;

import com.affycloud.TutorioBackend.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Entity
//@Table(name = "students")
@Table(
        name = "students",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_student_tenant_email", columnNames = {"tenant_id","email"}),
                @UniqueConstraint(name = "uq_student_tenant_phone", columnNames = {"tenant_id","phone_number"})
        }
)
public class Student {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(length = 128)
    private String name;

    @Column(length = 512)
    private String email;

    @Column(length = 15)
    private String phoneNumber;

    @Column
    private Date dateOfBirth;

    @Column(length = 15)
    private Long age;

    @Column(length = 70)
    private String password;

    @Column(name = "country_code", length = 5)
    @Pattern(
            regexp = "^\\+[1-9][0-9]{0,3}$",
            message = "Country code must start with + followed by 1 to 4 digits (example: +91)"
    )
    private String countryCode;

      @JsonIgnore
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cart> cartItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Role role;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "fire_base_token")
    private String fireBaseToken;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;


    @Column
    @CreationTimestamp(source = SourceType.DB)
    private Date createdAt;


    @Column
    @UpdateTimestamp(source = SourceType.DB)
    private Date updatedAt;

    public void addToCart(CourseAttempt courseAttempt) {
        Cart cart = new Cart();
        cart.setStudent(this);
        cart.setCourseAttempt(courseAttempt);
        this.cartItems.add(cart);
    }
    
    public void removeFromCart(CourseAttempt courseAttempt) {
        Cart cartToRemove = null;
        for (Cart cart : this.cartItems) {
            if (cart.getCourseAttempt().equals(courseAttempt)) {
                cartToRemove = cart;
                break;
            }
        }
        if (cartToRemove != null) {
            this.cartItems.remove(cartToRemove);
            cartToRemove.setStudent(null);
        }
    }
}
