package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.application.service.ValidationService;
import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import com.vehiclecare.vehiclecaremicro.domain.port.in.UpdateVehicleUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.BusinessValidationException;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.ResourceNotFoundException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateVehicleUseCaseImpl implements UpdateVehicleUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final ValidationService validationService;

    @Override
    @Transactional
    public Vehicle updateVehicle(String vehicleId, String userId, Vehicle vehicle) {
        log.info("Updating vehicle vehicleId={} userId={}", vehicleId, userId);
        Optional<Vehicle> existingOptional = vehicleRepositoryPort.findByIdAndUserId(vehicleId, userId);
        Vehicle existing = existingOptional.orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado"));

        if (vehicle.getUserId() != null && !vehicle.getUserId().equals(existing.getUserId())) {
            log.warn("Vehicle update rejected due to ownership mismatch vehicleId={} existingUserId={} requestUserId={}",
                    vehicleId, existing.getUserId(), vehicle.getUserId());
            throw new BusinessValidationException("El vehículo no pertenece al usuario");
        }

        validationService.normalizeAndValidateVehicle(vehicle);
        existing.setBrand(vehicle.getBrand());
        existing.setModel(vehicle.getModel());
        existing.setYear(vehicle.getYear());
        existing.setLicensePlate(vehicle.getLicensePlate());
        existing.setVin(vehicle.getVin());
        existing.setCurrentKilometers(vehicle.getCurrentKilometers());
        existing.setFuelType(vehicle.getFuelType());

        Vehicle saved = vehicleRepositoryPort.save(existing);
        log.info("Vehicle updated successfully vehicleId={} userId={}", saved.getId(), userId);
        return saved;
    }
}
