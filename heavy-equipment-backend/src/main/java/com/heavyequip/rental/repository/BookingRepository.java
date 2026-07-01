package com.heavyequip.rental.repository;

import com.heavyequip.rental.entity.Booking;
import com.heavyequip.rental.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            SELECT b FROM Booking b
            WHERE b.equipment.equipmentId = :equipmentId
              AND b.status IN :activeStatuses
              AND b.startDateTime < :newEnd
              AND b.endDateTime > :newStart
            """)
    List<Booking> findConflictingBookings(
            @Param("equipmentId") Long equipmentId,
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd,
            @Param("activeStatuses") List<BookingStatus> activeStatuses
    );

    Page<Booking> findByCustomer_UserId(Long customerId, Pageable pageable);

    Page<Booking> findByEquipment_Owner_UserId(Long ownerId, Pageable pageable);

    boolean existsByEquipment_EquipmentIdAndStatusIn(Long equipmentId, List<BookingStatus> statuses);
}