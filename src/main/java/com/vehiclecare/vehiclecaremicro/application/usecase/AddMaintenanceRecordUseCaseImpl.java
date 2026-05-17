package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.application.service.ValidationService;
import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.port.in.AddMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.MaintenanceRepositoryPort;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddMaintenanceRecordUseCaseImpl implements AddMaintenanceRecordUseCase {

    private final MaintenanceRepositoryPort maintenanceRepositoryPort;
    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final ValidationService validationService;

    @Override
    @Transactional
    public MaintenanceRecord addMaintenanceRecord(String vehicleId, String userId, MaintenanceRecord maintenanceRecord) {
        log.info("Creating maintenance record vehicleId={} userId={} category={} date={}",
                vehicleId, userId, maintenanceRecord.getCategory(), maintenanceRecord.getDate());
        vehicleRepositoryPort.findByIdAndUserId(vehicleId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));
        maintenanceRecord.setVehicleId(vehicleId);
        validationService.normalizeAndValidateMaintenance(maintenanceRecord);
        if (maintenanceRecord.getId() == null || maintenanceRecord.getId().isBlank()) {
            maintenanceRecord.setId(UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        }
        MaintenanceRecord saved = maintenanceRepositoryPort.save(maintenanceRecord);
        log.info("Maintenance record created successfully recordId={} vehicleId={} userId={}",
                saved.getId(), vehicleId, userId);
        return saved;
    }
}
