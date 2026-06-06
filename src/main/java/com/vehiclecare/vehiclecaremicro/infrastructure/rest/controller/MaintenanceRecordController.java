package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.dto.request.MaintenanceRecordCreateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.request.MaintenanceRecordUpdateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.AttachmentResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.MaintenanceRecordResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.MaintenanceAttachmentService;
import com.vehiclecare.vehiclecaremicro.application.service.PublicFileUrlService;
import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.AttachmentMapper;
import com.vehiclecare.vehiclecaremicro.domain.port.in.AddMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.DeleteMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.GetMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.ListMaintenanceRecordsUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.UpdateMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.MaintenanceRecordMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.security.AuthenticationContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

/**
 * REST controller for vehicle maintenance records and their attachments.
 *
 * <p>This controller exposes CRUD operations for maintenance records, plus upload,
 * download and deletion endpoints for related attachments. All endpoints operate in
 * the context of the authenticated user and delegate business rules to application
 * services and use cases.</p>
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
@Slf4j
public class MaintenanceRecordController {

    private final AddMaintenanceRecordUseCase addMaintenanceRecordUseCase;
    private final GetMaintenanceRecordUseCase getMaintenanceRecordUseCase;
    private final ListMaintenanceRecordsUseCase listMaintenanceRecordsUseCase;
    private final UpdateMaintenanceRecordUseCase updateMaintenanceRecordUseCase;
    private final DeleteMaintenanceRecordUseCase deleteMaintenanceRecordUseCase;
    private final MaintenanceRecordMapper maintenanceRecordMapper;
    private final MaintenanceAttachmentService maintenanceAttachmentService;
    private final AttachmentMapper attachmentMapper;
    private final PublicFileUrlService publicFileUrlService;
    private final AuthenticationContext authenticationContext;

    @GetMapping("/vehicles/{vehicleId}/records")
    public ResponseEntity<List<MaintenanceRecordResponseDTO>> listByVehicle(
            @PathVariable("vehicleId") String vehicleId,
            HttpServletRequest request
    ) {
        log.info("List maintenance records request vehicleId={} authenticatedUserId={}", vehicleId, authenticatedUserId(request));
        List<MaintenanceRecordResponseDTO> records = listMaintenanceRecordsUseCase.listByVehicleId(
                        vehicleId,
                        authenticatedUserId(request)
                )
                .stream()
                .map(record -> toResponse(record, request))
                .toList();
        log.info("Maintenance records listed vehicleId={} count={} authenticatedUserId={}",
                vehicleId, records.size(), authenticatedUserId(request));
        return ResponseEntity.ok(records);
    }

    @GetMapping("/records/{id}")
    public ResponseEntity<MaintenanceRecordResponseDTO> getRecord(@PathVariable("id") String id, HttpServletRequest request) {
        log.info("Get maintenance record request recordId={} authenticatedUserId={}", id, authenticatedUserId(request));
        return getMaintenanceRecordUseCase.getById(id, authenticatedUserId(request))
                .map(record -> toResponse(record, request))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/vehicles/{vehicleId}/records")
    public ResponseEntity<MaintenanceRecordResponseDTO> addMaintenance(
            @PathVariable("vehicleId") String vehicleId,
            @Valid @RequestBody MaintenanceRecordCreateRequestDTO requestDTO,
            HttpServletRequest request
    ) {
        log.info("Create maintenance record request vehicleId={} authenticatedUserId={} category={} date={}",
                vehicleId, authenticatedUserId(request), requestDTO.getCategory(), requestDTO.getDate());
        MaintenanceRecord maintenance = maintenanceRecordMapper.toDomain(requestDTO);
        MaintenanceRecord created = addMaintenanceRecordUseCase.addMaintenanceRecord(
                vehicleId,
                authenticatedUserId(request),
                maintenance
        );
        log.info("Maintenance record created recordId={} vehicleId={} authenticatedUserId={}",
                created.getId(), vehicleId, authenticatedUserId(request));
        return new ResponseEntity<>(toResponse(created, request), HttpStatus.CREATED);
    }

    @PutMapping("/records/{id}")
    public ResponseEntity<MaintenanceRecordResponseDTO> updateRecord(
            @PathVariable("id") String id,
            @Valid @RequestBody MaintenanceRecordUpdateRequestDTO requestDTO,
            HttpServletRequest request
    ) {
        log.info("Update maintenance record request recordId={} authenticatedUserId={} category={} date={}",
                id, authenticatedUserId(request), requestDTO.getCategory(), requestDTO.getDate());
        MaintenanceRecord maintenance = maintenanceRecordMapper.toDomain(requestDTO);
        MaintenanceRecord updated = updateMaintenanceRecordUseCase.update(id, authenticatedUserId(request), maintenance);
        log.info("Maintenance record updated recordId={} authenticatedUserId={}", updated.getId(), authenticatedUserId(request));
        return ResponseEntity.ok(toResponse(updated, request));
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable("id") String id, HttpServletRequest request) {
        log.info("Delete maintenance record request recordId={} authenticatedUserId={}", id, authenticatedUserId(request));
        boolean deleted = deleteMaintenanceRecordUseCase.delete(id, authenticatedUserId(request));
        if (!deleted) {
            log.warn("Maintenance record not found for deletion recordId={} authenticatedUserId={}", id, authenticatedUserId(request));
            return ResponseEntity.notFound().build();
        }
        log.info("Maintenance record deleted recordId={} authenticatedUserId={}", id, authenticatedUserId(request));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/records/{id}/attachments")
    public ResponseEntity<List<AttachmentResponseDTO>> uploadAttachments(
            @PathVariable("id") String id,
            @RequestParam("files") MultipartFile[] files,
            HttpServletRequest request
    ) {
        log.info("Upload maintenance attachments request recordId={} authenticatedUserId={} filesCount={}",
                id, authenticatedUserId(request), files == null ? 0 : files.length);
        List<AttachmentResponseDTO> attachments = maintenanceAttachmentService.upload(
                        id,
                        authenticatedUserId(request),
                        files
                )
                .stream()
                .map(attachmentMapper::toResponse)
                .map(attachment -> toAttachmentResponse(attachment, request))
                .toList();
        log.info("Maintenance attachments uploaded recordId={} uploadedCount={} authenticatedUserId={}",
                id, attachments.size(), authenticatedUserId(request));
        return new ResponseEntity<>(attachments, HttpStatus.CREATED);
    }

    @GetMapping("/records/{recordId}/attachments/{attachmentId}/download")
    public ResponseEntity<InputStreamResource> downloadAttachment(
            @PathVariable("recordId") String recordId,
            @PathVariable("attachmentId") String attachmentId,
            HttpServletRequest request
    ) {
        String userId = authenticatedUserId(request);
        log.info("Download maintenance attachment request recordId={} attachmentId={} authenticatedUserId={}",
                recordId, attachmentId, userId);
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
            HttpServletRequest request
    ) {
        log.info("Delete maintenance attachment request recordId={} attachmentId={} authenticatedUserId={}",
                recordId, attachmentId, authenticatedUserId(request));
        maintenanceAttachmentService.delete(recordId, attachmentId, authenticatedUserId(request));
        log.info("Maintenance attachment deleted recordId={} attachmentId={} authenticatedUserId={}",
                recordId, attachmentId, authenticatedUserId(request));
        return ResponseEntity.noContent().build();
    }

    private String authenticatedUserId(HttpServletRequest request) {
        return authenticationContext.requireCurrentUser(request).userId();
    }

    private MaintenanceRecordResponseDTO toResponse(MaintenanceRecord record, HttpServletRequest request) {
        MaintenanceRecordResponseDTO response = maintenanceRecordMapper.toResponse(record);
        response.setAttachments(
                response.getAttachments()
                        .stream()
                        .map(attachment -> toAttachmentResponse(attachment, request))
                        .toList()
        );
        return response;
    }

    private AttachmentResponseDTO toAttachmentResponse(AttachmentResponseDTO attachment, HttpServletRequest request) {
        attachment.setFilePath(publicFileUrlService.buildObjectUrl(request, attachment.getFilePath()));
        return attachment;
    }
}
