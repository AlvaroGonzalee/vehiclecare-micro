package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.application.service.ValidationService;
import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import com.vehiclecare.vehiclecaremicro.domain.port.in.CreateVehicleUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.UserRepositoryPort;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service that creates vehicles for an existing user.
 *
 * <p>The implementation verifies that the owner exists, assigns ownership to the
 * new vehicle, delegates normalization and validation to the shared validation service
 * and finally persists the vehicle through the outbound repository port.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateVehicleUseCaseImpl implements CreateVehicleUseCase {

    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final ValidationService validationService;

    /**
     * Creates and persists a vehicle for the given user.
     *
     * @param userId identifier of the owning user
     * @param vehicle vehicle data to create
     * @return persisted vehicle with its generated identifier when needed
     */
    @Override
    @Transactional
    public Vehicle createVehicle(String userId, Vehicle vehicle) {
        log.info("Creating vehicle for userId={} brand={} model={} year={}",
                userId, vehicle.getBrand(), vehicle.getModel(), vehicle.getYear());
        userRepositoryPort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        vehicle.setUserId(userId);
        validationService.normalizeAndValidateVehicle(vehicle);
        if (vehicle.getId() == null || vehicle.getId().isBlank()) {
            vehicle.setId(UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        }
        Vehicle saved = vehicleRepositoryPort.save(vehicle);
        log.info("Vehicle created successfully vehicleId={} userId={}", saved.getId(), userId);
        return saved;
    }
}
