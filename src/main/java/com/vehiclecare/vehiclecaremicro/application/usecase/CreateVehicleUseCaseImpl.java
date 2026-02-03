package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import com.vehiclecare.vehiclecaremicro.domain.port.in.CreateVehicleUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.UserRepositoryPort;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateVehicleUseCaseImpl implements CreateVehicleUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public CreateVehicleUseCaseImpl(VehicleRepositoryPort vehicleRepositoryPort,
                                    UserRepositoryPort userRepositoryPort) {
        this.vehicleRepositoryPort = vehicleRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    @Transactional
    public Vehicle createVehicle(String userId, Vehicle vehicle) {
        userRepositoryPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        vehicle.setUserId(userId);
        return vehicleRepositoryPort.save(vehicle);
    }
}
