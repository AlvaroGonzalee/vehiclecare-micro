package com.vehiclecare.vehiclecaremicro.domain.port.in;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;

public interface AddMaintenanceRecordUseCase {
    MaintenanceRecord addMaintenanceRecord(String vehicleId, String userId, MaintenanceRecord maintenanceRecord);
}
