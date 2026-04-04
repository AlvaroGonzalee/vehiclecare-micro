package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.port.in.GetMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.MaintenanceRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetMaintenanceRecordUseCaseImpl implements GetMaintenanceRecordUseCase {

    private final MaintenanceRepositoryPort maintenanceRepositoryPort;

    @Override
    public Optional<MaintenanceRecord> getById(String recordId, String userId) {
        return maintenanceRepositoryPort.findByIdAndUserId(recordId, userId);
    }
}
