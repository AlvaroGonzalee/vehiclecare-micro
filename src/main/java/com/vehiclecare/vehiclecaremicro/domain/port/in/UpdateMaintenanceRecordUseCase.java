package com.vehiclecare.vehiclecaremicro.domain.port.in;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;

public interface UpdateMaintenanceRecordUseCase {
    MaintenanceRecord update(String recordId, String userId, MaintenanceRecord maintenanceRecord);
}
