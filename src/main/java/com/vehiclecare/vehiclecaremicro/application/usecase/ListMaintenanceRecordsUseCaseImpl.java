package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.port.in.ListMaintenanceRecordsUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.MaintenanceRepositoryPort;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListMaintenanceRecordsUseCaseImpl implements ListMaintenanceRecordsUseCase {

    private final MaintenanceRepositoryPort maintenanceRepositoryPort;
    private final VehicleRepositoryPort vehicleRepositoryPort;

    @Override
    public List<MaintenanceRecord> listByVehicleId(String vehicleId, String userId) {
        vehicleRepositoryPort.findByIdAndUserId(vehicleId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        return maintenanceRepositoryPort.findByVehicleIdAndUserId(vehicleId, userId);
    }
}
