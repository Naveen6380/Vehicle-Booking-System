package com.heavyequip.rental.entity;

/**
 * Lifecycle states of a booking. See SDD section "State Diagram for Booking".
 *
 * PENDING -> APPROVED -> COMPLETED
 * PENDING -> REJECTED
 * PENDING -> CANCELLED (by customer)
 */
public enum BookingStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED,
    COMPLETED
}
