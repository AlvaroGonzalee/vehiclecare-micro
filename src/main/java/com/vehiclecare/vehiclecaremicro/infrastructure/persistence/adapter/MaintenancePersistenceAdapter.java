package com.vehiclecare.vehiclecaremicro.infrastructure.persistence.adapter;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.port.out.MaintenanceRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.MaintenanceRecordMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.MaintenanceRecordEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.MaintenanceRecordJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

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

    @Override
    public List<MaintenanceRecord> findByVehicleIdAndUserId(String vehicleId, String userId) {
        return maintenanceRecordJpaRepository.findByVehicle_IdAndVehicle_User_Id(vehicleId, userId)
                .stream()
                .map(maintenanceRecordMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<MaintenanceRecord> findById(String id) {
        return maintenanceRecordJpaRepository.findById(id)
                .map(maintenanceRecordMapper::toDomain);
    }

    @Override
    public Optional<MaintenanceRecord> findByIdAndUserId(String id, String userId) {
        return maintenanceRecordJpaRepository.findByIdAndVehicle_User_Id(id, userId)
                .map(maintenanceRecordMapper::toDomain);
    }

    @Override
    public void deleteById(String id) {
        maintenanceRecordJpaRepository.deleteById(id);
    }
}
