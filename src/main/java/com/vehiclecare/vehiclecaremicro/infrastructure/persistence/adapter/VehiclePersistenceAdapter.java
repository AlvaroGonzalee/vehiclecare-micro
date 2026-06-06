package com.vehiclecare.vehiclecaremicro.infrastructure.persistence.adapter;

import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.VehicleMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.VehicleEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.VehicleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Persistence adapter that bridges the vehicle repository port with Spring Data JPA.
 *
 * <p>The adapter converts between the domain {@link Vehicle} model and the JPA
 * {@link VehicleEntity} representation. It keeps the application layer isolated from
 * persistence details while delegating actual data access to {@link VehicleJpaRepository}.</p>
 */
@Component
@RequiredArgsConstructor
public class VehiclePersistenceAdapter implements VehicleRepositoryPort {

    private final VehicleJpaRepository vehicleJpaRepository;
    private final VehicleMapper vehicleMapper;

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
    public Optional<Vehicle> findByIdAndUserId(String id, String userId) {
        return vehicleJpaRepository.findByIdAndUser_Id(id, userId).map(vehicleMapper::toDomain);
    }

    @Override
    public List<Vehicle> findByUserId(String userId) {
        return vehicleJpaRepository.findByUser_Id(userId)
                .stream()
                .map(vehicleMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        vehicleJpaRepository.deleteById(id);
    }
}
