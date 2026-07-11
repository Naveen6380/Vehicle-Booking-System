package com.heavyequip.rental.service;

import com.heavyequip.rental.dto.request.BookingRequestDTO;
import com.heavyequip.rental.dto.response.BookingResponseDTO;
import com.heavyequip.rental.entity.Booking;
import com.heavyequip.rental.entity.BookingStatus;
import com.heavyequip.rental.entity.Equipment;
import com.heavyequip.rental.entity.EquipmentStatus;
import com.heavyequip.rental.entity.User;
import com.heavyequip.rental.exception.BookingConflictException;
import com.heavyequip.rental.exception.ResourceNotFoundException;
import com.heavyequip.rental.exception.UnauthorizedActionException;
import com.heavyequip.rental.repository.BookingRepository;
import com.heavyequip.rental.repository.EquipmentRepository;
import com.heavyequip.rental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    /**
     * Customer booking request submit பண்றது.
     *
     * Steps (interview-ல இதை exactly சொல்லு):
     * 1. Customer-ஐ DB-ல find பண்ணு (JWT email use பண்ணி)
     * 2. Equipment exist பண்றதா, AVAILABLE-ஆ இருக்கான்னு check பண்ணு
     * 3. Start time < End time validate பண்ணு
     * 4. OVERLAP CHECK — same equipment-ல conflicting booking இருக்கான்னு DB query
     * 5. Total price calculate பண்ணு (hours × pricePerHour)
     * 6. Booking save பண்ணு with status = PENDING
     */
    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO dto, String customerEmail) {

        // Step 1: Customer find
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        // Step 2: Equipment find + status check
        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Equipment not found with id: " + dto.getEquipmentId()));

        if (equipment.getStatus() == EquipmentStatus.UNAVAILABLE) {
            throw new BookingConflictException(
                    "This equipment is currently unavailable for booking");
        }

        // Step 3: Date validation
        if (!dto.getStartDateTime().isBefore(dto.getEndDateTime())) {
            throw new IllegalArgumentException(
                    "Start time must be before end time");
        }

        // Step 4: THE CORE LOGIC — Double booking prevention!
        // PENDING + APPROVED bookings மட்டும் check பண்றோம்
        // REJECTED/CANCELLED bookings slot-ஐ block பண்றதில்ல
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                equipment.getEquipmentId(),
                dto.getStartDateTime(),
                dto.getEndDateTime(),
                List.of(BookingStatus.PENDING, BookingStatus.APPROVED)
        );

        if (!conflicts.isEmpty()) {
            throw new BookingConflictException(
                    "Equipment is already booked for this time slot. " +
                            "Please choose a different time.");
        }

        // Step 5: Price calculation
        // Duration.between → hours calculate பண்ணு
        // partial hour இருந்தா round up (ex: 2h 30min → 3 hours charge)
        long hours = Duration.between(
                dto.getStartDateTime(),
                dto.getEndDateTime()
        ).toHours();

        long remainingMinutes = Duration.between(
                dto.getStartDateTime(),
                dto.getEndDateTime()
        ).toMinutesPart();

        if (remainingMinutes > 0) {
            hours += 1; // Partial hour → full hour-ஆ charge
        }

        BigDecimal totalPrice = equipment.getPricePerHour()
                .multiply(BigDecimal.valueOf(hours));

        // Step 6: Booking entity build + save
        Booking booking = Booking.builder()
                .equipment(equipment)
                .customer(customer)
                .startDateTime(dto.getStartDateTime())
                .endDateTime(dto.getEndDateTime())
                .totalPrice(totalPrice)
                .status(BookingStatus.PENDING)
                .build();

        Booking saved = bookingRepository.save(booking);
        return mapToResponse(saved);
    }

    /**
     * Owner booking-ஐ APPROVE பண்றது.
     *
     * Important: Approve பண்ணும் போதும் overlap re-check பண்றோம்!
     * ஏன்னா: இரண்டு PENDING bookings same slot-ல இருக்கலாம்,
     * ஒன்னை approve பண்றாங்க, அப்புறம் இன்னொன்னையும் approve பண்ண try பண்றாங்க —
     * அதை தடுக்கணும்.
     */
    @Transactional
    public BookingResponseDTO approveBooking(Long bookingId, String ownerEmail) {

        Booking booking = getBookingById(bookingId);

        // இந்த equipment இந்த owner-ஓடதா?
        if (!booking.getEquipment().getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedActionException(
                    "You can only approve bookings for your own equipment");
        }

        // PENDING-ஆ இருக்கான்னு check
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new UnauthorizedActionException(
                    "Only PENDING bookings can be approved. Current status: "
                            + booking.getStatus());
        }

        // Re-check overlap at approval time (double safety!)
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                booking.getEquipment().getEquipmentId(),
                booking.getStartDateTime(),
                booking.getEndDateTime(),
                List.of(BookingStatus.APPROVED) // Already approved bookings check
        );

        // Current booking-ஐயே conflict list-லிருந்து exclude பண்ணு
        conflicts.removeIf(b -> b.getBookingId().equals(bookingId));

        if (!conflicts.isEmpty()) {
            throw new BookingConflictException(
                    "Cannot approve: Another booking already approved for this time slot");
        }

        booking.setStatus(BookingStatus.APPROVED);
        return mapToResponse(bookingRepository.save(booking));
    }

    /**
     * Owner booking-ஐ REJECT பண்றது.
     */
    @Transactional
    public BookingResponseDTO rejectBooking(Long bookingId, String ownerEmail) {

        Booking booking = getBookingById(bookingId);

        if (!booking.getEquipment().getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedActionException(
                    "You can only reject bookings for your own equipment");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new UnauthorizedActionException(
                    "Only PENDING bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        return mapToResponse(bookingRepository.save(booking));
    }

    /**
     * Customer தன்னோட PENDING booking-ஐ cancel பண்றது.
     * APPROVED booking cancel பண்ண முடியாது (owner-கிட்ட contact பண்ணணும்).
     */
    @Transactional
    public BookingResponseDTO cancelBooking(Long bookingId, String customerEmail) {

        Booking booking = getBookingById(bookingId);

        if (!booking.getCustomer().getEmail().equals(customerEmail)) {
            throw new UnauthorizedActionException(
                    "You can only cancel your own bookings");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new UnauthorizedActionException(
                    "Only PENDING bookings can be cancelled. " +
                            "Contact the owner for APPROVED bookings.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        return mapToResponse(bookingRepository.save(booking));
    }

    /**
     * Customer-ன் booking history (My Bookings page).
     */
    public Page<BookingResponseDTO> getMyBookings(String customerEmail, Pageable pageable) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return bookingRepository
                .findByCustomer_UserId(customer.getUserId(), pageable)
                .map(this::mapToResponse);
    }

    /**
     * Owner-க்கு வந்த booking requests (Owner dashboard).
     */
    public Page<BookingResponseDTO> getBookingsForOwner(String ownerEmail, Pageable pageable) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        return bookingRepository
                .findByEquipment_Owner_UserId(owner.getUserId(), pageable)
                .map(this::mapToResponse);
    }

    // ---- Helper methods ----

    private Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Booking not found with id: " + id));
    }

    private BookingResponseDTO mapToResponse(Booking booking) {
        return BookingResponseDTO.builder()
                .bookingId(booking.getBookingId())
                .equipmentId(booking.getEquipment().getEquipmentId())
                .equipmentName(booking.getEquipment().getName())
                .customerName(booking.getCustomer().getName())
                .startDateTime(booking.getStartDateTime())
                .endDateTime(booking.getEndDateTime())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }
}