package com.heavyequip.rental.controller;

import com.heavyequip.rental.dto.request.EquipmentRequestDTO;
import com.heavyequip.rental.dto.response.EquipmentResponseDTO;
import com.heavyequip.rental.entity.EquipmentType;
import com.heavyequip.rental.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    /**
     * GET /api/v1/equipment
     * Public — யாரும் equipment browse பண்ணலாம்
     * Optional filters: ?type=JCB&location=Chennai&page=0&size=10
     *
     * @AuthenticationPrincipal — JwtFilter set பண்ணின email automatically வரும்
     */
    @GetMapping
    public ResponseEntity<Page<EquipmentResponseDTO>> searchEquipment(
            @RequestParam(required = false) EquipmentType type,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        return ResponseEntity.ok(
                equipmentService.searchEquipment(type, location, pageable));
    }

    /**
     * GET /api/v1/equipment/{id}
     * Public — single equipment details (booking form page-க்கு)
     */
    @GetMapping("/{id}")
    public ResponseEntity<EquipmentResponseDTO> getEquipment(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentDetails(id));
    }

    /**
     * POST /api/v1/equipment
     * OWNER only — equipment add பண்றது
     * @AuthenticationPrincipal → JWT-ல இருக்கிற email (JwtFilter set பண்ணினது)
     */
    @PostMapping
    public ResponseEntity<EquipmentResponseDTO> addEquipment(
            @Valid @RequestBody EquipmentRequestDTO dto,
            @AuthenticationPrincipal String ownerEmail) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(equipmentService.addEquipment(dto, ownerEmail));
    }

    /**
     * PUT /api/v1/equipment/{id}
     * OWNER only — equipment update பண்றது
     */
    @PutMapping("/{id}")
    public ResponseEntity<EquipmentResponseDTO> updateEquipment(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentRequestDTO dto,
            @AuthenticationPrincipal String ownerEmail) {

        return ResponseEntity.ok(
                equipmentService.updateEquipment(id, dto, ownerEmail));
    }

    /**
     * DELETE /api/v1/equipment/{id}
     * OWNER only — active booking இருந்தா delete block ஆகும்
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(
            @PathVariable Long id,
            @AuthenticationPrincipal String ownerEmail) {

        equipmentService.deleteEquipment(id, ownerEmail);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/equipment/my-listings
     * OWNER dashboard — தன்னோட equipment list
     */
    @GetMapping("/my-listings")
    public ResponseEntity<Page<EquipmentResponseDTO>> getMyEquipment(
            @AuthenticationPrincipal String ownerEmail,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        return ResponseEntity.ok(
                equipmentService.getMyEquipment(ownerEmail, pageable));
    }
}