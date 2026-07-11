package com.heavyequip.rental.controller;

import com.heavyequip.rental.dto.request.BookingRequestDTO;
import com.heavyequip.rental.dto.response.BookingResponseDTO;
import com.heavyequip.rental.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * POST /api/v1/bookings
     * CUSTOMER — booking request submit பண்றது
     */
    @PostMapping
    public ResponseEntity<BookingResponseDTO> createBooking(
            @Valid @RequestBody BookingRequestDTO dto,
            @AuthenticationPrincipal String customerEmail) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(dto, customerEmail));
    }

    /**
     * PUT /api/v1/bookings/{id}/approve
     * OWNER — booking approve பண்றது
     */
    @PutMapping("/{id}/approve")
    public ResponseEntity<BookingResponseDTO> approveBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal String ownerEmail) {

        return ResponseEntity.ok(bookingService.approveBooking(id, ownerEmail));
    }

    /**
     * PUT /api/v1/bookings/{id}/reject
     * OWNER — booking reject பண்றது
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<BookingResponseDTO> rejectBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal String ownerEmail) {

        return ResponseEntity.ok(bookingService.rejectBooking(id, ownerEmail));
    }

    /**
     * PUT /api/v1/bookings/{id}/cancel
     * CUSTOMER — pending booking cancel பண்றது
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingResponseDTO> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal String customerEmail) {

        return ResponseEntity.ok(bookingService.cancelBooking(id, customerEmail));
    }

    /**
     * GET /api/v1/bookings/my-bookings
     * CUSTOMER — "My Bookings" page
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<Page<BookingResponseDTO>> getMyBookings(
            @AuthenticationPrincipal String customerEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        return ResponseEntity.ok(
                bookingService.getMyBookings(customerEmail, pageable));
    }

    /**
     * GET /api/v1/bookings/owner-requests
     * OWNER — "Booking Requests" dashboard
     */
    @GetMapping("/owner-requests")
    public ResponseEntity<Page<BookingResponseDTO>> getOwnerBookings(
            @AuthenticationPrincipal String ownerEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        return ResponseEntity.ok(
                bookingService.getBookingsForOwner(ownerEmail, pageable));
    }
}