package com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository;

import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.MaintenanceRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceRecordJpaRepository extends JpaRepository<MaintenanceRecordEntity, String> {
    List<MaintenanceRecordEntity> findByVehicle_Id(String vehicleId);
}
