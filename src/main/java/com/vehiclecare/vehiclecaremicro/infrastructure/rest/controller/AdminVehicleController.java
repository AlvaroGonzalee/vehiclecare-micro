package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.dto.response.VehicleResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.PublicFileUrlService;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.VehicleMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.VehicleJpaRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/vehicles")
@RequiredArgsConstructor
public class AdminVehicleController {

    private final VehicleJpaRepository vehicleJpaRepository;
    private final VehicleMapper vehicleMapper;
    private final PublicFileUrlService publicFileUrlService;

    @GetMapping
    public ResponseEntity<List<VehicleResponseDTO>> listVehicles(
            @RequestParam(value = "userId", required = false) String userId,
            HttpServletRequest request
    ) {
        List<VehicleResponseDTO> vehicles = (userId == null || userId.isBlank()
                        ? vehicleJpaRepository.findAllByOrderByIdDesc()
                        : vehicleJpaRepository.findByUser_IdOrderByIdDesc(userId))
                .stream()
                .map(vehicleMapper::toDomain)
                .map(vehicleMapper::toResponse)
                .map(vehicle -> toResponse(vehicle, request))
                .toList();
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponseDTO> getVehicle(@PathVariable("id") String id, HttpServletRequest request) {
        return vehicleJpaRepository.findById(id)
                .map(vehicleMapper::toDomain)
                .map(vehicleMapper::toResponse)
                .map(vehicle -> toResponse(vehicle, request))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private VehicleResponseDTO toResponse(VehicleResponseDTO vehicle, HttpServletRequest request) {
        vehicle.setImageUrl(publicFileUrlService.buildObjectUrl(request, vehicle.getImageUrl()));
        return vehicle;
    }
}
