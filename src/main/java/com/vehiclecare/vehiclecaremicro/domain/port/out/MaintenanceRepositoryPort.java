package com.vehiclecare.vehiclecaremicro.domain.port.out;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;

import java.util.List;
import java.util.Optional;

public interface MaintenanceRepositoryPort {
    MaintenanceRecord save(MaintenanceRecord maintenanceRecord);
    List<MaintenanceRecord> findByVehicleId(String vehicleId);
    Optional<MaintenanceRecord> findById(String id);
    void deleteById(String id);
}
