package com.heavyequip.rental.exception;

/**
 * Thrown when a user tries to perform an action they are not allowed to.
 * Example: Owner tries to approve someone else's equipment booking,
 *          or Customer tries to edit equipment.
 * Returns: HTTP 403 Forbidden
 */
public class UnauthorizedActionException extends RuntimeException {

    public UnauthorizedActionException(String message) {
        super(message);
    }
}