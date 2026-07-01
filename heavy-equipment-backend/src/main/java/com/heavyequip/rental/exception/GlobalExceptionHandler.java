package com.heavyequip.rental.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Central place for ALL exception handling in the application.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 * It intercepts exceptions thrown from ANY controller/service
 * and converts them into clean JSON error responses.
 *
 * Without this: Spring gives ugly HTML error pages or
 * raw stack traces to the frontend — very bad UX and security risk.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles: Equipment/User/Booking not found
     * Returns: HTTP 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex) {

        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles: Overlapping booking time slots
     * Returns: HTTP 409
     */
    @ExceptionHandler(BookingConflictException.class)
    public ResponseEntity<Map<String, Object>> handleBookingConflict(
            BookingConflictException ex) {

        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Handles: Duplicate email during registration
     * Returns: HTTP 409
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(
            DuplicateEmailException ex) {

        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Handles: @Valid annotation failures (blank fields, invalid email, weak password etc.)
     * Returns: HTTP 400 with a map of {fieldName: errorMessage}
     *
     * Example response:
     * {
     *   "email": "Enter a valid email address",
     *   "password": "Password must be at least 8 characters"
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Handles: User tries to do something outside their role/ownership
     * Returns: HTTP 403
     */
    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(
            UnauthorizedActionException ex) {

        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /**
     * Handles: Any unexpected exception not caught above (safety net)
     * Returns: HTTP 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong. Please try again later."
        );
    }

    // Helper method — builds a standard error JSON structure
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String message) {

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", status.value());
        errorBody.put("message", message);
        errorBody.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(status).body(errorBody);
    }
}