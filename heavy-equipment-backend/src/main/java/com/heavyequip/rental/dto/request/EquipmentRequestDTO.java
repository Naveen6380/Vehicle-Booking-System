package com.heavyequip.rental.dto.request;

import com.heavyequip.rental.entity.EquipmentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class EquipmentRequestDTO {

    @NotBlank(message = "Equipment name is required")
    @Size(max = 100)
    private String name;

    @NotNull(message = "Equipment type is required")
    private EquipmentType type;

    @NotBlank(message = "Location is required")
    @Size(max = 150)
    private String location;

    @NotNull(message = "Price per hour is required")
    @DecimalMin(value = "1.0", message = "Price must be at least ₹1 per hour")
    private BigDecimal pricePerHour;

    private String imageUrl;
}