package com.vehiclecare.vehiclecaremicro.domain.port.in;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import java.util.Optional;

public interface GetMaintenanceRecordUseCase {
    Optional<MaintenanceRecord> getById(String recordId);
}
