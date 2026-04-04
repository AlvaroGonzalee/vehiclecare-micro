package com.vehiclecare.vehiclecaremicro.domain.port.in;

import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import java.util.Optional;

public interface GetVehicleUseCase {
    Optional<Vehicle> getVehicleById(String id, String userId);
}
