package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.port.in.GetMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.MaintenanceRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.ResourceNotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetMaintenanceRecordUseCaseImpl implements GetMaintenanceRecordUseCase {

    private final MaintenanceRepositoryPort maintenanceRepositoryPort;

    @Override
    public Optional<MaintenanceRecord> getById(String recordId, String userId) {
        Optional<MaintenanceRecord> record = maintenanceRepositoryPort.findByIdAndUserId(recordId, userId);
        if (record.isEmpty()) {
            throw new ResourceNotFoundException("Registro no encontrado");
        }
        return record;
    }
}
