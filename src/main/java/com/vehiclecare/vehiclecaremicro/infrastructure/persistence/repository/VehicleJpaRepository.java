package com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository;

import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleJpaRepository extends JpaRepository<VehicleEntity, String> {
    List<VehicleEntity> findByUser_Id(String userId);
    List<VehicleEntity> findByUser_IdOrderByIdDesc(String userId);
    List<VehicleEntity> findAllByOrderByIdDesc();
    java.util.Optional<VehicleEntity> findByIdAndUser_Id(String id, String userId);
}
