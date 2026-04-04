package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import com.vehiclecare.vehiclecaremicro.domain.port.in.GetVehicleUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.ResourceNotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetVehicleUseCaseImpl implements GetVehicleUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;

    @Override
    public Optional<Vehicle> getVehicleById(String id, String userId) {
        Optional<Vehicle> vehicle = vehicleRepositoryPort.findByIdAndUserId(id, userId);
        if (vehicle.isEmpty()) {
            throw new ResourceNotFoundException("Vehículo no encontrado");
        }
        return vehicle;
    }
}
