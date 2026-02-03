package com.vehiclecare.vehiclecaremicro.domain.port.in;

import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;

public interface CreateVehicleUseCase {
    Vehicle createVehicle(String userId, Vehicle vehicle);
}
