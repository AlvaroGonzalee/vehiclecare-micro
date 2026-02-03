package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.port.in.AddMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.MaintenanceRepositoryPort;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddMaintenanceRecordUseCaseImpl implements AddMaintenanceRecordUseCase {

    private final MaintenanceRepositoryPort maintenanceRepositoryPort;
    private final VehicleRepositoryPort vehicleRepositoryPort;

    public AddMaintenanceRecordUseCaseImpl(MaintenanceRepositoryPort maintenanceRepositoryPort,
                                           VehicleRepositoryPort vehicleRepositoryPort) {
        this.maintenanceRepositoryPort = maintenanceRepositoryPort;
        this.vehicleRepositoryPort = vehicleRepositoryPort;
    }

    @Override
    @Transactional
    public MaintenanceRecord addMaintenanceRecord(String vehicleId, MaintenanceRecord maintenanceRecord) {
        vehicleRepositoryPort.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        maintenanceRecord.setVehicleId(vehicleId);
        return maintenanceRepositoryPort.save(maintenanceRecord);
    }
}
