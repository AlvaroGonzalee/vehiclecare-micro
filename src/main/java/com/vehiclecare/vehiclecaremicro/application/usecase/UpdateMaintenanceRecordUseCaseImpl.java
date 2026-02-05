package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.port.in.UpdateMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.MaintenanceRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateMaintenanceRecordUseCaseImpl implements UpdateMaintenanceRecordUseCase {

    private final MaintenanceRepositoryPort maintenanceRepositoryPort;
    @Override
    @Transactional
    public MaintenanceRecord update(String recordId, MaintenanceRecord maintenanceRecord) {
        Optional<MaintenanceRecord> existingOptional = maintenanceRepositoryPort.findById(recordId);
        MaintenanceRecord existing = existingOptional.orElseThrow(() -> new IllegalArgumentException("Registro no encontrado"));

        if (maintenanceRecord.getVehicleId() != null && !maintenanceRecord.getVehicleId().equals(existing.getVehicleId())) {
            throw new IllegalArgumentException("El registro no pertenece al vehículo");
        }

        existing.setTitle(maintenanceRecord.getTitle());
        existing.setDate(maintenanceRecord.getDate());
        existing.setCategory(maintenanceRecord.getCategory());
        existing.setKilometers(maintenanceRecord.getKilometers());
        existing.setPrice(maintenanceRecord.getPrice());
        existing.setDescription(maintenanceRecord.getDescription());
        return maintenanceRepositoryPort.save(existing);
    }
}
