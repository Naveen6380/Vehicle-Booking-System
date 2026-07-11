package com.heavyequip.rental.service;

import com.heavyequip.rental.dto.request.EquipmentRequestDTO;
import com.heavyequip.rental.dto.response.EquipmentResponseDTO;
import com.heavyequip.rental.entity.BookingStatus;
import com.heavyequip.rental.entity.Equipment;
import com.heavyequip.rental.entity.EquipmentStatus;
import com.heavyequip.rental.entity.EquipmentType;
import com.heavyequip.rental.entity.User;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    /**
     * Owner புதுசா equipment add பண்றது.
     * ownerEmail → JWT token-லிருந்து Controller எடுத்து pass பண்ணும்.
     */
    @Transactional
    public EquipmentResponseDTO addEquipment(EquipmentRequestDTO dto, String ownerEmail) {

        // JWT-ல இருக்கிற email வச்சு Owner-ஐ DB-ல தேடு
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        // Equipment entity build பண்ணு
        Equipment equipment = Equipment.builder()
                .owner(owner)
                .name(dto.getName())
                .type(dto.getType())
                .location(dto.getLocation())
                .pricePerHour(dto.getPricePerHour())
                .imageUrl(dto.getImageUrl())
                .status(EquipmentStatus.AVAILABLE)
                .build();

        Equipment saved = equipmentRepository.save(equipment);
        return mapToResponse(saved);
    }

    /**
     * Equipment update பண்றது — Owner மட்டும் தன்னோட equipment-ஐ edit பண்ணலாம்.
     */
    @Transactional
    public EquipmentResponseDTO updateEquipment(Long equipmentId,
                                                EquipmentRequestDTO dto,
                                                String ownerEmail) {

        Equipment equipment = getEquipmentById(equipmentId);

        // இந்த equipment இந்த owner-ஓடதா? வேற ஆள் equipment-ஐ edit பண்ண முயற்சித்தா block
        if (!equipment.getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedActionException(
                    "You can only edit your own equipment");
        }

        equipment.setName(dto.getName());
        equipment.setType(dto.getType());
        equipment.setLocation(dto.getLocation());
        equipment.setPricePerHour(dto.getPricePerHour());
        equipment.setImageUrl(dto.getImageUrl());

        return mapToResponse(equipmentRepository.save(equipment));
    }

    /**
     * Equipment delete பண்றது.
     * Business Rule: Active booking இருந்தா delete பண்ண முடியாது!
     */
    @Transactional
    public void deleteEquipment(Long equipmentId, String ownerEmail) {

        Equipment equipment = getEquipmentById(equipmentId);

        // Owner verification
        if (!equipment.getOwner().getEmail().equals(ownerEmail)) {
            throw new UnauthorizedActionException(
                    "You can only delete your own equipment");
        }

        // Active booking இருக்கானு check — இருந்தா delete block
        boolean hasActiveBookings = bookingRepository
                .existsByEquipment_EquipmentIdAndStatusIn(
                        equipmentId,
                        List.of(BookingStatus.PENDING, BookingStatus.APPROVED)
                );

        if (hasActiveBookings) {
            throw new UnauthorizedActionException(
                    "Cannot delete equipment with active bookings");
        }

        equipmentRepository.delete(equipment);
    }

    /**
     * Customer equipment list பாக்கறது — type/location filter-உடன், paginated.
     *
     * EquipmentType null-ஆ இருந்தா → எல்லா type-உம் காட்டு
     * location null-ஆ இருந்தா → எல்லா location-உம் காட்டு
     */
    public Page<EquipmentResponseDTO> searchEquipment(EquipmentType type,
                                                      String location,
                                                      Pageable pageable) {

        // Dynamic filter: type மட்டும், location மட்டும், இரண்டும், இல்லன்னா எல்லாம்
        if (type != null && location != null) {
            return equipmentRepository
                    .findByTypeAndLocationContainingIgnoreCaseAndStatus(
                            type, location, EquipmentStatus.AVAILABLE, pageable)
                    .map(this::mapToResponse);
        } else if (type != null) {
            return equipmentRepository
                    .findByTypeAndStatus(type, EquipmentStatus.AVAILABLE, pageable)
                    .map(this::mapToResponse);
        } else if (location != null) {
            return equipmentRepository
                    .findByLocationContainingIgnoreCaseAndStatus(
                            location, EquipmentStatus.AVAILABLE, pageable)
                    .map(this::mapToResponse);
        } else {
            return equipmentRepository
                    .findByStatus(EquipmentStatus.AVAILABLE, pageable)
                    .map(this::mapToResponse);
        }
    }

    /**
     * Single equipment details பாக்கறது (booking form-ல price பாக்க).
     */
    public EquipmentResponseDTO getEquipmentDetails(Long equipmentId) {
        return mapToResponse(getEquipmentById(equipmentId));
    }

    /**
     * Owner-ன் equipment list (Owner dashboard).
     */
    public Page<EquipmentResponseDTO> getMyEquipment(String ownerEmail, Pageable pageable) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found"));

        return equipmentRepository
                .findByOwner_UserId(owner.getUserId(), pageable)
                .map(this::mapToResponse);
    }

    // ---- Helper methods ----

    private Equipment getEquipmentById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Equipment not found with id: " + id));
    }

    /**
     * Entity → ResponseDTO conversion (mapping).
     * Entity-ஐ directly return பண்றதில்ல — DTO மட்டும் expose பண்றோம்.
     */
    private EquipmentResponseDTO mapToResponse(Equipment equipment) {
        return EquipmentResponseDTO.builder()
                .equipmentId(equipment.getEquipmentId())
                .name(equipment.getName())
                .type(equipment.getType())
                .location(equipment.getLocation())
                .pricePerHour(equipment.getPricePerHour())
                .imageUrl(equipment.getImageUrl())
                .status(equipment.getStatus())
                .ownerName(equipment.getOwner().getName())
                .createdAt(equipment.getCreatedAt())
                .build();
    }
}
