package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.application.service.ValidationService;
import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import com.vehiclecare.vehiclecaremicro.domain.port.in.CreateVehicleUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.UserRepositoryPort;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateVehicleUseCaseImpl implements CreateVehicleUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final ValidationService validationService;

    @Override
    @Transactional
    public Vehicle createVehicle(String userId, Vehicle vehicle) {
        userRepositoryPort.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        vehicle.setUserId(userId);
        validationService.normalizeAndValidateVehicle(vehicle);
        if (vehicle.getId() == null || vehicle.getId().isBlank()) {
            vehicle.setId(UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        }
        return vehicleRepositoryPort.save(vehicle);
    }
}
