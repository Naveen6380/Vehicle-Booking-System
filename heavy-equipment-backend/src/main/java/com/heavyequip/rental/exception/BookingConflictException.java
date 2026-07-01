package com.heavyequip.rental.exception;

/**
 * Thrown when a new booking overlaps with an existing PENDING or APPROVED booking
 * for the same equipment. This is the core "double booking prevention" exception.
 * GlobalExceptionHandler catches this and returns HTTP 409 Conflict.
 *
 * Example usage in BookingService:
 *   if (!conflicts.isEmpty()) {
 *       throw new BookingConflictException("Equipment already booked for this time slot");
 *   }
 */
public class BookingConflictException extends RuntimeException {

    public BookingConflictException(String message) {
        super(message);
    }
}