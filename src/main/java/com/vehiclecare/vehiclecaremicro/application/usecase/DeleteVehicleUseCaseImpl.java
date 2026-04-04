package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.domain.port.in.DeleteVehicleUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteVehicleUseCaseImpl implements DeleteVehicleUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;

    @Override
    @Transactional
    public boolean deleteVehicle(String vehicleId, String userId) {
        if (vehicleRepositoryPort.findByIdAndUserId(vehicleId, userId).isEmpty()) {
            throw new ResourceNotFoundException("Vehículo no encontrado");
        }
        vehicleRepositoryPort.deleteById(vehicleId);
        return true;
    }
}
