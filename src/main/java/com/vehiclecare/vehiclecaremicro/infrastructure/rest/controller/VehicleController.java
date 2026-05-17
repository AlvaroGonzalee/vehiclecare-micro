package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.dto.request.VehicleCreateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.request.VehicleUpdateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.FileUploadResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.VehicleResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.MinioStorageService;
import com.vehiclecare.vehiclecaremicro.application.service.PublicFileUrlService;
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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class VehicleController {

    private final CreateVehicleUseCase createVehicleUseCase;
    private final GetVehicleUseCase getVehicleUseCase;
    private final ListVehiclesUseCase listVehiclesUseCase;
    private final UpdateVehicleUseCase updateVehicleUseCase;
    private final DeleteVehicleUseCase deleteVehicleUseCase;
    private final VehicleRepositoryPort vehicleRepositoryPort;
    private final MinioStorageService minioStorageService;
    private final PublicFileUrlService publicFileUrlService;
    private final VehicleMapper vehicleMapper;
    private final AuthenticationContext authenticationContext;

    @GetMapping
    public ResponseEntity<List<VehicleResponseDTO>> listVehicles(
            @RequestParam(value = "userId", required = false) String userId,
            HttpServletRequest request
    ) {
        String authenticatedUserId = authenticatedUserId(request);
        log.info("List vehicles request queryUserId={} authenticatedUserId={}", userId, authenticatedUserId);
        if (userId != null && !userId.equals(authenticatedUserId)) {
            log.warn("Ownership violation while listing vehicles queryUserId={} authenticatedUserId={}",
                    userId, authenticatedUserId);
            throw new OwnershipAccessException("No puedes listar vehículos de otro usuario");
        }

        List<VehicleResponseDTO> vehicles = listVehiclesUseCase.listByUserId(authenticatedUserId)
                .stream()
                .map(vehicle -> toResponse(vehicle, request))
                .toList();
        log.info("Vehicles listed count={} authenticatedUserId={}", vehicles.size(), authenticatedUserId);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> getVehicle(@PathVariable("id") String id, HttpServletRequest request) {
        log.info("Get vehicle request vehicleId={} authenticatedUserId={}", id, authenticatedUserId(request));
        return getVehicleUseCase.getVehicleById(id, authenticatedUserId(request))
                .map(vehicle -> toResponse(vehicle, request))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<VehicleResponseDTO> createVehicle(
            @Valid @RequestBody VehicleCreateRequestDTO requestDTO,
            HttpServletRequest request
    ) {
        String authenticatedUserId = authenticatedUserId(request);
        log.info("Create vehicle request authenticatedUserId={} brand={} model={} year={}",
                authenticatedUserId, requestDTO.getBrand(), requestDTO.getModel(), requestDTO.getYear());
        if (!requestDTO.getUserId().equals(authenticatedUserId)) {
            log.warn("Ownership violation while creating vehicle requestUserId={} authenticatedUserId={}",
                    requestDTO.getUserId(), authenticatedUserId);
            throw new OwnershipAccessException("No puedes crear vehículos para otro usuario");
        }
        Vehicle vehicle = vehicleMapper.toDomain(requestDTO);
        Vehicle created = createVehicleUseCase.createVehicle(authenticatedUserId, vehicle);
        log.info("Vehicle created vehicleId={} authenticatedUserId={}", created.getId(), authenticatedUserId);
        return new ResponseEntity<>(toResponse(created, request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> updateVehicle(
            @PathVariable("id") String id,
            @Valid @RequestBody VehicleUpdateRequestDTO requestDTO,
            HttpServletRequest request
    ) {
        log.info("Update vehicle request vehicleId={} authenticatedUserId={} year={} licensePlate={}",
                id, authenticatedUserId(request), requestDTO.getYear(), requestDTO.getLicensePlate());
        Vehicle vehicle = vehicleMapper.toDomain(requestDTO);
        Vehicle updated = updateVehicleUseCase.updateVehicle(id, authenticatedUserId(request), vehicle);
        log.info("Vehicle updated vehicleId={} authenticatedUserId={}", updated.getId(), authenticatedUserId(request));
        return ResponseEntity.ok(toResponse(updated, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable("id") String id, HttpServletRequest request) {
        log.info("Delete vehicle request vehicleId={} authenticatedUserId={}", id, authenticatedUserId(request));
        boolean deleted = deleteVehicleUseCase.deleteVehicle(id, authenticatedUserId(request));
        if (!deleted) {
            log.warn("Vehicle not found for deletion vehicleId={} authenticatedUserId={}", id, authenticatedUserId(request));
            return ResponseEntity.notFound().build();
        }
        log.info("Vehicle deleted vehicleId={} authenticatedUserId={}", id, authenticatedUserId(request));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/image")
    public ResponseEntity<VehicleResponseDTO> uploadVehicleImage(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        log.info("Upload vehicle image request vehicleId={} authenticatedUserId={} originalFileName={} size={}",
                id, authenticatedUserId(request), file.getOriginalFilename(), file.getSize());
        Vehicle vehicle = vehicleRepositoryPort.findByIdAndUserId(id, authenticatedUserId(request))
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        FileUploadResponseDTO upload = minioStorageService.uploadImage(file, "vehicles/" + id);
        vehicle.setImageUrl(upload.getObjectKey());
        Vehicle saved = vehicleRepositoryPort.save(vehicle);
        log.info("Vehicle image updated vehicleId={} objectKey={}", saved.getId(), upload.getObjectKey());
        return ResponseEntity.ok(toResponse(saved, request));
    }

    private VehicleResponseDTO toResponse(Vehicle vehicle, HttpServletRequest request) {
        VehicleResponseDTO response = vehicleMapper.toResponse(vehicle);
        response.setImageUrl(publicFileUrlService.buildObjectUrl(request, response.getImageUrl()));
        return response;
    }

    private String authenticatedUserId(HttpServletRequest request) {
        return authenticationContext.requireCurrentUser(request).userId();
    }
}
