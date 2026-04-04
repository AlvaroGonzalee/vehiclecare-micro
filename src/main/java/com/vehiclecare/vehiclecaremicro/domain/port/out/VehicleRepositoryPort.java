package com.vehiclecare.vehiclecaremicro.domain.port.out;

import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;

import java.util.List;
import java.util.Optional;

public interface VehicleRepositoryPort {
    Vehicle save(Vehicle vehicle);
    Optional<Vehicle> findById(String id);
    Optional<Vehicle> findByIdAndUserId(String id, String userId);
    List<Vehicle> findByUserId(String userId);
    void deleteById(String id);
}
