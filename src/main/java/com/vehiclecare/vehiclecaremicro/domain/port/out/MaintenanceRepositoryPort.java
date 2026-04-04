package com.vehiclecare.vehiclecaremicro.domain.port.out;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRepositoryPort {
    MaintenanceRecord save(MaintenanceRecord maintenanceRecord);
    List<MaintenanceRecord> findByVehicleId(String vehicleId);
    List<MaintenanceRecord> findByVehicleIdAndUserId(String vehicleId, String userId);
    Optional<MaintenanceRecord> findById(String id);
    Optional<MaintenanceRecord> findByIdAndUserId(String id, String userId);
    void deleteById(String id);
}
