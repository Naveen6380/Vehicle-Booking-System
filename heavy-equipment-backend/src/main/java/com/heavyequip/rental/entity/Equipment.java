package com.heavyequip.rental.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a piece of heavy equipment listed for rent by an Owner.
 */
@Entity
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_id")
    private Long equipmentId;

    /**
     * The Owner who listed this equipment.
     * FetchType.LAZY: we don't want to load the full User object every time
     * we fetch equipment in a list - only when explicitly needed (e.g. equipment.getOwner().getName()).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private EquipmentType type;

    @Column(name = "location", nullable = false, length = 150)
    private String location;

    @Column(name = "price_per_hour", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private EquipmentStatus status = EquipmentStatus.AVAILABLE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ---- Relationships ----

    /**
     * All bookings (across all time periods, any status) made for this equipment.
     * Used by the overlap-checking logic in BookingService.
     */
    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = EquipmentStatus.AVAILABLE;
        }
    }
}
