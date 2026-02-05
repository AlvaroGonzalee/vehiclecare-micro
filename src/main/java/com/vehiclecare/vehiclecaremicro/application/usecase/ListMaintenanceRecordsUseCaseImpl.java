package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.port.in.ListMaintenanceRecordsUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.MaintenanceRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListMaintenanceRecordsUseCaseImpl implements ListMaintenanceRecordsUseCase {

    private final MaintenanceRepositoryPort maintenanceRepositoryPort;

    @Override
    public List<MaintenanceRecord> listByVehicleId(String vehicleId) {
        return maintenanceRepositoryPort.findByVehicleId(vehicleId);
    }
}
