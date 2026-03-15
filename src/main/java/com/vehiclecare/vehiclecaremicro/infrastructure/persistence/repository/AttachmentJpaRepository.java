package com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository;

import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.AttachmentEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentJpaRepository extends JpaRepository<AttachmentEntity, String> {
    Optional<AttachmentEntity> findByIdAndMaintenanceRecord_Id(String id, String maintenanceRecordId);
}
