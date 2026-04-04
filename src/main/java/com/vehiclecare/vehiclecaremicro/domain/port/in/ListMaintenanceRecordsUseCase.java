package com.vehiclecare.vehiclecaremicro.domain.port.in;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import java.util.List;

public interface ListMaintenanceRecordsUseCase {
    List<MaintenanceRecord> listByVehicleId(String vehicleId, String userId);
}
