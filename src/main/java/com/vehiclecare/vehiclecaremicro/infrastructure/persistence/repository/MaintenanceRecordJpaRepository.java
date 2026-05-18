package com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository;

import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.MaintenanceRecordEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRecordJpaRepository extends JpaRepository<MaintenanceRecordEntity, String> {

    @EntityGraph(attributePaths = "attachments")
    List<MaintenanceRecordEntity> findByVehicle_Id(String vehicleId);

    @EntityGraph(attributePaths = "attachments")
    List<MaintenanceRecordEntity> findByVehicle_IdAndVehicle_User_Id(String vehicleId, String userId);

    @EntityGraph(attributePaths = "attachments")
    Optional<MaintenanceRecordEntity> findByIdAndVehicle_User_Id(String id, String userId);
}
