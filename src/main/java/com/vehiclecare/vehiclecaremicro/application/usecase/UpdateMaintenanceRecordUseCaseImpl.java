package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.application.service.ValidationService;
import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.port.in.UpdateMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.MaintenanceRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.BusinessValidationException;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.ResourceNotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateMaintenanceRecordUseCaseImpl implements UpdateMaintenanceRecordUseCase {

    private final MaintenanceRepositoryPort maintenanceRepositoryPort;
    private final ValidationService validationService;
    @Override
    @Transactional
    public MaintenanceRecord update(String recordId, String userId, MaintenanceRecord maintenanceRecord) {
        Optional<MaintenanceRecord> existingOptional = maintenanceRepositoryPort.findByIdAndUserId(recordId, userId);
        MaintenanceRecord existing = existingOptional.orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado"));

        if (maintenanceRecord.getVehicleId() != null && !maintenanceRecord.getVehicleId().equals(existing.getVehicleId())) {
            throw new BusinessValidationException("El registro no pertenece al vehículo");
        }

        validationService.normalizeAndValidateMaintenance(maintenanceRecord);
        existing.setTitle(maintenanceRecord.getTitle());
        existing.setDate(maintenanceRecord.getDate());
        existing.setCategory(maintenanceRecord.getCategory());
        existing.setKilometers(maintenanceRecord.getKilometers());
        existing.setPrice(maintenanceRecord.getPrice());
        existing.setDescription(maintenanceRecord.getDescription());
        return maintenanceRepositoryPort.save(existing);
    }
}
