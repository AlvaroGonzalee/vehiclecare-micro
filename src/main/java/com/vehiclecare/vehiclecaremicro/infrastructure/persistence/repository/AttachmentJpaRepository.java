package com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository;

import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.AttachmentEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AttachmentJpaRepository extends JpaRepository<AttachmentEntity, String> {
    Optional<AttachmentEntity> findByIdAndMaintenanceRecord_Id(String id, String maintenanceRecordId);

    @Query("""
            select a.filePath
            from AttachmentEntity a
            where a.maintenanceRecord.vehicle.user.id = :userId
            """)
    List<String> findFilePathsByUserId(String userId);
}
