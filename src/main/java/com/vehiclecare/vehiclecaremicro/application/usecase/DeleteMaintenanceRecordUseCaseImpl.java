package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.application.service.MaintenanceAttachmentService;
import com.vehiclecare.vehiclecaremicro.domain.port.in.DeleteMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.MaintenanceRepositoryPort;
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
    public boolean delete(String recordId) {
        if (maintenanceRepositoryPort.findById(recordId).isEmpty()) {
            return false;
        }
        maintenanceAttachmentService.deleteAllFromRecord(recordId);
        maintenanceRepositoryPort.deleteById(recordId);
        return true;
    }
}
