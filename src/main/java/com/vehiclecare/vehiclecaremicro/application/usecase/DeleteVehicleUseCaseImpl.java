package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.domain.port.in.DeleteVehicleUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
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
            return false;
        }
        vehicleRepositoryPort.deleteById(vehicleId);
        return true;
    }
}
