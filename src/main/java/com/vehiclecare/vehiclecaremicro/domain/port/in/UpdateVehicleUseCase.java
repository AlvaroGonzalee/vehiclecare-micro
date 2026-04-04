package com.vehiclecare.vehiclecaremicro.domain.port.in;

import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;

public interface UpdateVehicleUseCase {
    Vehicle updateVehicle(String vehicleId, String userId, Vehicle vehicle);
}
