package com.heavyequip.rental.repository;

import com.heavyequip.rental.entity.Equipment;
import com.heavyequip.rental.entity.EquipmentStatus;
import com.heavyequip.rental.entity.EquipmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long>,
        JpaSpecificationExecutor<Equipment> {

    List<Equipment> findByType(EquipmentType type);

    Page<Equipment> findByOwner_UserId(Long ownerId, Pageable pageable);

    // Search filter combinations (Service-ல எந்த filter select ஆகுதுன்னு பொறுத்து call ஆகும்)
    Page<Equipment> findByStatus(EquipmentStatus status, Pageable pageable);

    Page<Equipment> findByTypeAndStatus(EquipmentType type,
                                        EquipmentStatus status,
                                        Pageable pageable);

    Page<Equipment> findByLocationContainingIgnoreCaseAndStatus(String location,
                                                                EquipmentStatus status,
                                                                Pageable pageable);

    Page<Equipment> findByTypeAndLocationContainingIgnoreCaseAndStatus(EquipmentType type,
                                                                       String location,
                                                                       EquipmentStatus status,
                                                                       Pageable pageable);
}