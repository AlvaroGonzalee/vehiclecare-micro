package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.dto.request.MaintenanceRecordCreateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.request.MaintenanceRecordUpdateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.AttachmentResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.MaintenanceRecordResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.MaintenanceAttachmentService;
import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.AttachmentMapper;
import com.vehiclecare.vehiclecaremicro.domain.port.in.AddMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.DeleteMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.GetMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.ListMaintenanceRecordsUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.UpdateMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.MaintenanceRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
public class MaintenanceRecordController {

    private final AddMaintenanceRecordUseCase addMaintenanceRecordUseCase;
    private final GetMaintenanceRecordUseCase getMaintenanceRecordUseCase;
    private final ListMaintenanceRecordsUseCase listMaintenanceRecordsUseCase;
    private final UpdateMaintenanceRecordUseCase updateMaintenanceRecordUseCase;
    private final DeleteMaintenanceRecordUseCase deleteMaintenanceRecordUseCase;
    private final MaintenanceRecordMapper maintenanceRecordMapper;
    private final MaintenanceAttachmentService maintenanceAttachmentService;
    private final AttachmentMapper attachmentMapper;

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
            @Valid @RequestBody MaintenanceRecordCreateRequestDTO requestDTO
    ) {
        MaintenanceRecord maintenance = maintenanceRecordMapper.toDomain(requestDTO);
        MaintenanceRecord created = addMaintenanceRecordUseCase.addMaintenanceRecord(vehicleId, maintenance);
        return new ResponseEntity<>(maintenanceRecordMapper.toResponse(created), HttpStatus.CREATED);
    }

    @PutMapping("/records/{id}")
    public ResponseEntity<MaintenanceRecordResponseDTO> updateRecord(
            @PathVariable("id") String id,
            @Valid @RequestBody MaintenanceRecordUpdateRequestDTO requestDTO
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

    @PostMapping("/records/{id}/attachments")
    public ResponseEntity<List<AttachmentResponseDTO>> uploadAttachments(
            @PathVariable("id") String id,
            @RequestHeader(MaintenanceAttachmentService.USER_ID_HEADER) String userId,
            @RequestParam("files") MultipartFile[] files
    ) {
        List<AttachmentResponseDTO> attachments = maintenanceAttachmentService.upload(id, userId, files)
                .stream()
                .map(attachmentMapper::toResponse)
                .toList();
        return new ResponseEntity<>(attachments, HttpStatus.CREATED);
    }

    @GetMapping("/records/{recordId}/attachments/{attachmentId}/download")
    public ResponseEntity<InputStreamResource> downloadAttachment(
            @PathVariable("recordId") String recordId,
            @PathVariable("attachmentId") String attachmentId,
            @RequestHeader(MaintenanceAttachmentService.USER_ID_HEADER) String userId
    ) {
        var attachment = maintenanceAttachmentService.getAttachment(recordId, attachmentId, userId);
        var stream = maintenanceAttachmentService.download(recordId, attachmentId, userId);
        String encodedName = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (attachment.getFileType() != null && !attachment.getFileType().isBlank()) {
            mediaType = MediaType.parseMediaType(attachment.getFileType());
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName
                )
                .body(new InputStreamResource(stream));
    }

    @DeleteMapping("/records/{recordId}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable("recordId") String recordId,
            @PathVariable("attachmentId") String attachmentId,
            @RequestHeader(MaintenanceAttachmentService.USER_ID_HEADER) String userId
    ) {
        maintenanceAttachmentService.delete(recordId, attachmentId, userId);
        return ResponseEntity.noContent().build();
    }
}
