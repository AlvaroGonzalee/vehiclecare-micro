package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import com.vehiclecare.vehiclecaremicro.domain.port.in.ListVehiclesUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListVehiclesUseCaseImpl implements ListVehiclesUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;

    @Override
    public List<Vehicle> listByUserId(String userId) {
        return vehicleRepositoryPort.findByUserId(userId);
    }
}
