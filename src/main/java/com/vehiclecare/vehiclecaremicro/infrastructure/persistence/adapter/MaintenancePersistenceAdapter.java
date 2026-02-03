package com.vehiclecare.vehiclecaremicro.infrastructure.persistence.adapter;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.port.out.MaintenanceRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.MaintenanceRecordMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.MaintenanceRecordEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.MaintenanceRecordJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MaintenancePersistenceAdapter implements MaintenanceRepositoryPort {

    private final MaintenanceRecordJpaRepository maintenanceRecordJpaRepository;
    private final MaintenanceRecordMapper maintenanceRecordMapper;

    @Override
    public MaintenanceRecord save(MaintenanceRecord maintenanceRecord) {
        MaintenanceRecordEntity entity = maintenanceRecordMapper.toEntity(maintenanceRecord);
        MaintenanceRecordEntity saved = maintenanceRecordJpaRepository.save(entity);
        return maintenanceRecordMapper.toDomain(saved);
    }

    @Override
    public List<MaintenanceRecord> findByVehicleId(String vehicleId) {
        return maintenanceRecordJpaRepository.findByVehicle_Id(vehicleId)
                .stream()
                .map(maintenanceRecordMapper::toDomain)
                .toList();
    }
}
