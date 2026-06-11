package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.dto.response.AdminMaintenanceRecordResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.AttachmentResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.PublicFileUrlService;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.AttachmentMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.MaintenanceRecordEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.MaintenanceRecordJpaRepository;
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
@RequestMapping("/admin/maintenance-records")
@RequiredArgsConstructor
public class AdminMaintenanceController {

    private final MaintenanceRecordJpaRepository maintenanceRecordJpaRepository;
    private final AttachmentMapper attachmentMapper;
    private final PublicFileUrlService publicFileUrlService;

    @GetMapping
    public ResponseEntity<List<AdminMaintenanceRecordResponseDTO>> listRecords(
            @RequestParam(value = "userId", required = false) String userId,
            HttpServletRequest request
    ) {
        List<AdminMaintenanceRecordResponseDTO> records = (userId == null || userId.isBlank()
                        ? maintenanceRecordJpaRepository.findAllByOrderByMaintenanceDateDesc()
                        : maintenanceRecordJpaRepository.findByVehicle_User_IdOrderByMaintenanceDateDesc(userId))
                .stream()
                .map(record -> toResponse(record, request))
                .toList();
        return ResponseEntity.ok(records);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminMaintenanceRecordResponseDTO> getRecord(
            @PathVariable("id") String id,
            HttpServletRequest request
    ) {
        return maintenanceRecordJpaRepository.findDetailedById(id)
                .map(record -> toResponse(record, request))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private AdminMaintenanceRecordResponseDTO toResponse(MaintenanceRecordEntity record, HttpServletRequest request) {
        List<AttachmentResponseDTO> attachments = record.getAttachments().stream()
                .map(attachmentMapper::toDomain)
                .map(attachmentMapper::toResponse)
                .map(attachment -> toAttachmentResponse(attachment, request))
                .toList();

        return new AdminMaintenanceRecordResponseDTO(
                record.getId(),
                record.getVehicle().getUser().getId(),
                record.getVehicle().getId(),
                record.getTitle(),
                record.getMaintenanceDate(),
                record.getCategory(),
                record.getKilometers(),
                record.getPrice(),
                record.getDescription(),
                attachments
        );
    }

    private AttachmentResponseDTO toAttachmentResponse(AttachmentResponseDTO attachment, HttpServletRequest request) {
        attachment.setFilePath(publicFileUrlService.buildObjectUrl(request, attachment.getFilePath()));
        return attachment;
    }
}
