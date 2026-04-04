package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.port.in.ListMaintenanceRecordsUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.MaintenanceRepositoryPort;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.ResourceNotFoundException;
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
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));
        return maintenanceRepositoryPort.findByVehicleIdAndUserId(vehicleId, userId);
    }
}
