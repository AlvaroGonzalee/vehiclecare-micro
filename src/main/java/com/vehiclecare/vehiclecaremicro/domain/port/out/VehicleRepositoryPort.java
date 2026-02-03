package com.vehiclecare.vehiclecaremicro.domain.port.out;

import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;

import java.util.List;
import java.util.Optional;

public interface VehicleRepositoryPort {
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(String id);
    List<Vehicle> findByUserId(String userId);
}
