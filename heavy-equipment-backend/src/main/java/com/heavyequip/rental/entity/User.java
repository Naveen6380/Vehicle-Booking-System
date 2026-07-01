package com.heavyequip.rental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a registered user of the platform.
 * A single User table is used for all three roles (Customer, Owner, Admin),
 * differentiated by the `role` column. This is simpler than separate tables
 * per role and works well since all roles share the same core attributes
 * (name, email, password, phone).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Stores the BCrypt-hashed password. Never store plain text here.
     * Mapped to "password" column but field name avoids confusion with raw input DTOs.
     */
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone", nullable = false, length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ---- Relationships ----

    /**
     * Equipment listings owned by this user (only populated/used when role = OWNER).
     * mappedBy = "owner" -> Equipment entity owns the FK column.
     * LAZY fetch to avoid loading all equipment every time a user is loaded.
     */
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Equipment> equipmentList = new ArrayList<>();

    /**
     * Bookings made by this user as a customer (only populated/used when role = CUSTOMER).
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
