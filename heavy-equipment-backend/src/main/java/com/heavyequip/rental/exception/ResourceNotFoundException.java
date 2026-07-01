package com.heavyequip.rental.exception;

/**
 * Thrown when a requested resource (Equipment, Booking, User) is not found in DB.
 * GlobalExceptionHandler catches this and returns HTTP 404.
 *
 * Example usage in Service:
 *   Equipment equipment = equipmentRepository.findById(id)
 *       .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}