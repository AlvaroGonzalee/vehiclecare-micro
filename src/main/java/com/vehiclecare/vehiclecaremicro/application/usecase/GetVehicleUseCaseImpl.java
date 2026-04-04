package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import com.vehiclecare.vehiclecaremicro.domain.port.in.GetVehicleUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetVehicleUseCaseImpl implements GetVehicleUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;

    @Override
    public Optional<Vehicle> getVehicleById(String id, String userId) {
        return vehicleRepositoryPort.findByIdAndUserId(id, userId);
    }
}
