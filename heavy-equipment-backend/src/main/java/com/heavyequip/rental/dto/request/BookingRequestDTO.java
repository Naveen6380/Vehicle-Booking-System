package com.heavyequip.rental.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingRequestDTO {

    @NotNull(message = "Equipment Id is required")
    private Long equipmentId;

    @NotNull(message = "Start date and time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date and time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endDateTime;

}
