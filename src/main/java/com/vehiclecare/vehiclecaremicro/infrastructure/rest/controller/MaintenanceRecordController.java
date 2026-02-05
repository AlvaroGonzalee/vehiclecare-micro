package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.dto.request.MaintenanceRecordRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.MaintenanceRecordResponseDTO;
import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.port.in.AddMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.DeleteMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.GetMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.ListMaintenanceRecordsUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.UpdateMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.MaintenanceRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class MaintenanceRecordController {

    private final AddMaintenanceRecordUseCase addMaintenanceRecordUseCase;
    private final GetMaintenanceRecordUseCase getMaintenanceRecordUseCase;
    private final ListMaintenanceRecordsUseCase listMaintenanceRecordsUseCase;
    private final UpdateMaintenanceRecordUseCase updateMaintenanceRecordUseCase;
    private final DeleteMaintenanceRecordUseCase deleteMaintenanceRecordUseCase;
    private final MaintenanceRecordMapper maintenanceRecordMapper;

    @GetMapping("/vehicles/{vehicleId}/records")
    public ResponseEntity<List<MaintenanceRecordResponseDTO>> listByVehicle(
            @PathVariable("vehicleId") String vehicleId
    ) {
        List<MaintenanceRecordResponseDTO> records = listMaintenanceRecordsUseCase.listByVehicleId(vehicleId)
                .stream()
                .map(maintenanceRecordMapper::toResponse)
                .toList();
        return ResponseEntity.ok(records);
    }

    @GetMapping("/records/{id}")
    public ResponseEntity<MaintenanceRecordResponseDTO> getRecord(@PathVariable("id") String id) {
        return getMaintenanceRecordUseCase.getById(id)
                .map(maintenanceRecordMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/vehicles/{vehicleId}/records")
    public ResponseEntity<MaintenanceRecordResponseDTO> addMaintenance(
            @PathVariable("vehicleId") String vehicleId,
            @RequestBody MaintenanceRecordRequestDTO requestDTO
    ) {
        MaintenanceRecord maintenance = maintenanceRecordMapper.toDomain(requestDTO);
        MaintenanceRecord created = addMaintenanceRecordUseCase.addMaintenanceRecord(vehicleId, maintenance);
        return new ResponseEntity<>(maintenanceRecordMapper.toResponse(created), HttpStatus.CREATED);
    }

    @PutMapping("/records/{id}")
    public ResponseEntity<MaintenanceRecordResponseDTO> updateRecord(
            @PathVariable("id") String id,
            @RequestBody MaintenanceRecordRequestDTO requestDTO
    ) {
        MaintenanceRecord maintenance = maintenanceRecordMapper.toDomain(requestDTO);
        MaintenanceRecord updated = updateMaintenanceRecordUseCase.update(id, maintenance);
        return ResponseEntity.ok(maintenanceRecordMapper.toResponse(updated));
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable("id") String id) {
        boolean deleted = deleteMaintenanceRecordUseCase.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
