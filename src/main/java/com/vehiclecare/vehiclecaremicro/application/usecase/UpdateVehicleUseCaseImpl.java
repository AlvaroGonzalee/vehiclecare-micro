package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import com.vehiclecare.vehiclecaremicro.domain.port.in.UpdateVehicleUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateVehicleUseCaseImpl implements UpdateVehicleUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;

    @Override
    @Transactional
    public Vehicle updateVehicle(String vehicleId, String userId, Vehicle vehicle) {
        Optional<Vehicle> existingOptional = vehicleRepositoryPort.findByIdAndUserId(vehicleId, userId);
        Vehicle existing = existingOptional.orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));

        if (vehicle.getUserId() != null && !vehicle.getUserId().equals(existing.getUserId())) {
            throw new IllegalArgumentException("El vehículo no pertenece al usuario");
        }

        existing.setBrand(vehicle.getBrand());
        existing.setModel(vehicle.getModel());
        existing.setYear(vehicle.getYear());
        existing.setLicensePlate(vehicle.getLicensePlate());
        existing.setVin(vehicle.getVin());
        existing.setCurrentKilometers(vehicle.getCurrentKilometers());
        existing.setFuelType(vehicle.getFuelType());

        return vehicleRepositoryPort.save(existing);
    }
}
