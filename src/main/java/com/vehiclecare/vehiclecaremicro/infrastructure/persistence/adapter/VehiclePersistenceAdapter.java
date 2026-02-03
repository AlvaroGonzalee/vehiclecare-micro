package com.vehiclecare.vehiclecaremicro.infrastructure.persistence.adapter;

import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.VehicleMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.VehicleEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.VehicleJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class VehiclePersistenceAdapter implements VehicleRepositoryPort {

    private final VehicleJpaRepository vehicleJpaRepository;
    private final VehicleMapper vehicleMapper;

    public VehiclePersistenceAdapter(VehicleJpaRepository vehicleJpaRepository, VehicleMapper vehicleMapper) {
        this.vehicleJpaRepository = vehicleJpaRepository;
        this.vehicleMapper = vehicleMapper;
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        VehicleEntity entity = vehicleMapper.toEntity(vehicle);
        VehicleEntity saved = vehicleJpaRepository.save(entity);
        return vehicleMapper.toDomain(saved);
    }

    @Override
    public Optional<Vehicle> findById(String id) {
        return vehicleJpaRepository.findById(id).map(vehicleMapper::toDomain);
    }

    @Override
    public List<Vehicle> findByUserId(String userId) {
        return vehicleJpaRepository.findByUser_Id(userId)
                .stream()
                .map(vehicleMapper::toDomain)
                .toList();
    }
}
