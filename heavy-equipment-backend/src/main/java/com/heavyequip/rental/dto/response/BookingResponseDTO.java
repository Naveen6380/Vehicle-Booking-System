package com.heavyequip.rental.dto.response;

import com.heavyequip.rental.entity.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponseDTO {

    private Long bookingId;
    private Long equipmentId;
    private String equipmentName;
    private String customerName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private LocalDateTime createdAt;
}