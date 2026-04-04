package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.dto.request.VehicleCreateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.request.VehicleUpdateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.FileUploadResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.VehicleResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.MinioStorageService;
import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import com.vehiclecare.vehiclecaremicro.domain.port.in.CreateVehicleUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.DeleteVehicleUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.GetVehicleUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.ListVehiclesUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.UpdateVehicleUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.VehicleMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.OwnershipAccessException;
import com.vehiclecare.vehiclecaremicro.infrastructure.security.AuthenticationContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
@Validated
public class VehicleController {

    private final CreateVehicleUseCase createVehicleUseCase;
    private final GetVehicleUseCase getVehicleUseCase;
    private final ListVehiclesUseCase listVehiclesUseCase;
    private final UpdateVehicleUseCase updateVehicleUseCase;
    private final DeleteVehicleUseCase deleteVehicleUseCase;
    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final MinioStorageService minioStorageService;
    private final VehicleMapper vehicleMapper;
    private final AuthenticationContext authenticationContext;

    @GetMapping
    public ResponseEntity<List<VehicleResponseDTO>> listVehicles(
            @RequestParam(value = "userId", required = false) String userId,
            HttpServletRequest request
    ) {
        String authenticatedUserId = authenticatedUserId(request);
        if (userId != null && !userId.equals(authenticatedUserId)) {
            throw new OwnershipAccessException("No puedes listar vehículos de otro usuario");
        }

        List<VehicleResponseDTO> vehicles = listVehiclesUseCase.listByUserId(authenticatedUserId)
                .stream()
                .map(vehicleMapper::toResponse)
                .toList();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> getVehicle(@PathVariable("id") String id, HttpServletRequest request) {
        return getVehicleUseCase.getVehicleById(id, authenticatedUserId(request))
                .map(vehicleMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<VehicleResponseDTO> createVehicle(
            @Valid @RequestBody VehicleCreateRequestDTO requestDTO,
            HttpServletRequest request
    ) {
        String authenticatedUserId = authenticatedUserId(request);
        if (!requestDTO.getUserId().equals(authenticatedUserId)) {
            throw new OwnershipAccessException("No puedes crear vehículos para otro usuario");
        }
        Vehicle vehicle = vehicleMapper.toDomain(requestDTO);
        Vehicle created = createVehicleUseCase.createVehicle(authenticatedUserId, vehicle);
        return new ResponseEntity<>(vehicleMapper.toResponse(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> updateVehicle(
            @PathVariable("id") String id,
            @Valid @RequestBody VehicleUpdateRequestDTO requestDTO,
            HttpServletRequest request
    ) {
        Vehicle vehicle = vehicleMapper.toDomain(requestDTO);
        Vehicle updated = updateVehicleUseCase.updateVehicle(id, authenticatedUserId(request), vehicle);
        return ResponseEntity.ok(vehicleMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable("id") String id, HttpServletRequest request) {
        boolean deleted = deleteVehicleUseCase.deleteVehicle(id, authenticatedUserId(request));
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<VehicleResponseDTO> uploadVehicleImage(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        Vehicle vehicle = vehicleRepositoryPort.findByIdAndUserId(id, authenticatedUserId(request))
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        FileUploadResponseDTO upload = minioStorageService.uploadImage(file, "vehicles/" + id);
        vehicle.setImageUrl(upload.getObjectUrl());
        Vehicle saved = vehicleRepositoryPort.save(vehicle);
        return ResponseEntity.ok(vehicleMapper.toResponse(saved));
    }

    private String authenticatedUserId(HttpServletRequest request) {
        return authenticationContext.requireCurrentUser(request).userId();
    }
}
