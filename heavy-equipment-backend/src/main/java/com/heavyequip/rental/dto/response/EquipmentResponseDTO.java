package com.heavyequip.rental.dto.response;

import com.heavyequip.rental.entity.EquipmentStatus;
import com.heavyequip.rental.entity.EquipmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentResponseDTO {

    private Long equipmentId;
    private String name;
    private EquipmentType type;
    private String location;
    private BigDecimal pricePerHour;
    private List<String> imageUrls;
    private EquipmentStatus status;
    private String ownerName;
    private LocalDateTime createdAt;
}