package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.application.service.MaintenanceAttachmentService;
import com.vehiclecare.vehiclecaremicro.domain.port.in.DeleteMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.MaintenanceRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteMaintenanceRecordUseCaseImpl implements DeleteMaintenanceRecordUseCase {

    private final MaintenanceRepositoryPort maintenanceRepositoryPort;
    private final MaintenanceAttachmentService maintenanceAttachmentService;

    @Override
    @Transactional
    public boolean delete(String recordId, String userId) {
        if (maintenanceRepositoryPort.findByIdAndUserId(recordId, userId).isEmpty()) {
            throw new ResourceNotFoundException("Registro no encontrado");
        }
        maintenanceAttachmentService.deleteAllFromRecord(recordId);
        maintenanceRepositoryPort.deleteById(recordId);
        return true;
    }
}
