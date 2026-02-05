package com.vehiclecare.vehiclecaremicro.domain.port.in;

import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import java.util.List;

public interface ListVehiclesUseCase {
    List<Vehicle> listByUserId(String userId);
}
