package com.affycloud.TutorioBackend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Table(name = "address")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Address {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank(message = "Street is required")
    @Column(length = 128, nullable = false)
    private String street;

    @NotBlank(message = "City is required")
    @Column(length = 128, nullable = false)
    private String city;

    @NotBlank(message = "State is required")
    @Column(length = 128, nullable = false)
    private String state;

    @NotBlank(message = "Postal Code is required")
    @Column(length = 10, nullable = false)
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Column(length = 128, nullable = false)
    private String country;

    @Column(nullable = false)
    private boolean isDeleted = false; 

    @NotNull(message = "Student is required")
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private boolean isDefault; 

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    private Date updatedAt;
}
