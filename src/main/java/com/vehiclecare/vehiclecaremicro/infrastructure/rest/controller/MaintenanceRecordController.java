package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.dto.request.MaintenanceRecordRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.MaintenanceRecordResponseDTO;
import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.port.in.AddMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.MaintenanceRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/maintenance-records")
@RequiredArgsConstructor
public class MaintenanceRecordController {

    private final AddMaintenanceRecordUseCase addMaintenanceRecordUseCase;
    private final MaintenanceRecordMapper maintenanceRecordMapper;

    @PostMapping
    public ResponseEntity<MaintenanceRecordResponseDTO> addMaintenance(@RequestBody MaintenanceRecordRequestDTO requestDTO) {
        MaintenanceRecord maintenance = maintenanceRecordMapper.toDomain(requestDTO);
        MaintenanceRecord created = addMaintenanceRecordUseCase.addMaintenanceRecord(requestDTO.getVehicleId(), maintenance);
        return new ResponseEntity<>(maintenanceRecordMapper.toResponse(created), HttpStatus.CREATED);
    }
}
