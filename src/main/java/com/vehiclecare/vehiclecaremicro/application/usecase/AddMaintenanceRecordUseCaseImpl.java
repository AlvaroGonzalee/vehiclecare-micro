package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.application.service.ValidationService;
import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.port.in.AddMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.MaintenanceRepositoryPort;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddMaintenanceRecordUseCaseImpl implements AddMaintenanceRecordUseCase {

    private final MaintenanceRepositoryPort maintenanceRepositoryPort;
    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final ValidationService validationService;

    @Override
    @Transactional
    public MaintenanceRecord addMaintenanceRecord(String vehicleId, String userId, MaintenanceRecord maintenanceRecord) {
        vehicleRepositoryPort.findByIdAndUserId(vehicleId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        maintenanceRecord.setVehicleId(vehicleId);
        validationService.normalizeAndValidateMaintenance(maintenanceRecord);
        if (maintenanceRecord.getId() == null || maintenanceRecord.getId().isBlank()) {
            maintenanceRecord.setId(UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        }
        return maintenanceRepositoryPort.save(maintenanceRecord);
    }
}
