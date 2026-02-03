package com.vehiclecare.vehiclecaremicro.domain.port.out;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;

import java.util.List;

public interface MaintenanceRepositoryPort {
    MaintenanceRecord save(MaintenanceRecord maintenanceRecord);
    List<MaintenanceRecord> findByVehicleId(String vehicleId);
}
