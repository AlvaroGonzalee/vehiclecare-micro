package com.vehiclecare.vehiclecaremicro.application.service;

import com.vehiclecare.vehiclecaremicro.domain.model.Attachment;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.BusinessValidationException;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.ResourceNotFoundException;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.AttachmentMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.AttachmentEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.MaintenanceRecordEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.AttachmentJpaRepository;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.MaintenanceRecordJpaRepository;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Manages file attachments linked to maintenance records.
 *
 * <p>This service coordinates ownership checks, repository access, object storage
 * operations and attachment mapping. It enforces attachment-specific business rules,
 * such as the maximum number of files allowed per maintenance record, and ensures
 * that only the owner of the related vehicle can access or modify those files.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceAttachmentService {

    public static final String USER_ID_HEADER = "X-User-Id";
    private static final int MAX_ATTACHMENTS_PER_RECORD = 2;

    private final MaintenanceRecordJpaRepository maintenanceRecordJpaRepository;
    private final AttachmentJpaRepository attachmentJpaRepository;
    private final AttachmentMapper attachmentMapper;
    private final MinioStorageService minioStorageService;

    /**
     * Uploads one or more attachments for a maintenance record owned by the given user.
     *
     * @param recordId maintenance record identifier
     * @param userId owner of the record
     * @param files files to upload
     * @return uploaded attachments mapped to the domain model
     */
    @Transactional
    public List<Attachment> upload(String recordId, String userId, MultipartFile[] files) {
        log.info("Uploading maintenance attachments recordId={} userId={} filesCount={}",
                recordId, userId, files == null ? 0 : files.length);
        if (files == null || files.length == 0) {
            throw new BusinessValidationException("Debes adjuntar al menos un archivo");
        }
        MaintenanceRecordEntity record = getOwnedRecord(recordId, userId);
        int currentAttachments = record.getAttachments() == null ? 0 : record.getAttachments().size();
        if (currentAttachments + files.length > MAX_ATTACHMENTS_PER_RECORD) {
            throw new BusinessValidationException("Solo puedes adjuntar un máximo de 2 archivos por registro");
        }

        List<Attachment> uploaded = new ArrayList<>();
        for (MultipartFile file : files) {
            var upload = minioStorageService.uploadDocument(file, "records/" + recordId);
            AttachmentEntity entity = AttachmentEntity.builder()
                    .id(generateId())
                    .maintenanceRecord(record)
                    .fileName(upload.getFileName())
                    .fileType(upload.getContentType())
                    .filePath(upload.getObjectKey())
                    .build();
            AttachmentEntity saved = attachmentJpaRepository.save(entity);
            uploaded.add(attachmentMapper.toDomain(saved));
        }
        log.info("Maintenance attachments uploaded recordId={} uploadedCount={} userId={}",
                recordId, uploaded.size(), userId);
        return uploaded;
    }

    /**
     * Retrieves attachment metadata after verifying record ownership.
     *
     * @param recordId maintenance record identifier
     * @param attachmentId attachment identifier
     * @param userId owner of the record
     * @return attachment metadata
     */
    @Transactional(readOnly = true)
    public Attachment getAttachment(String recordId, String attachmentId, String userId) {
        log.debug("Fetching maintenance attachment metadata recordId={} attachmentId={} userId={}",
                recordId, attachmentId, userId);
        getOwnedRecord(recordId, userId);
        return attachmentMapper.toDomain(getRecordAttachment(recordId, attachmentId));
    }

    /**
     * Opens a stream to download a stored attachment after authorization checks.
     *
     * @param recordId maintenance record identifier
     * @param attachmentId attachment identifier
     * @param userId owner of the record
     * @return input stream for the stored file
     */
    @Transactional(readOnly = true)
    public InputStream download(String recordId, String attachmentId, String userId) {
        log.info("Downloading maintenance attachment recordId={} attachmentId={} userId={}",
                recordId, attachmentId, userId);
        AttachmentEntity attachment = getAttachmentEntity(recordId, attachmentId, userId);
        return minioStorageService.download(attachment.getFilePath());
    }

    /**
     * Deletes a single attachment from both object storage and the relational database.
     *
     * @param recordId maintenance record identifier
     * @param attachmentId attachment identifier
     * @param userId owner of the record
     */
    @Transactional
    public void delete(String recordId, String attachmentId, String userId) {
        log.info("Deleting maintenance attachment recordId={} attachmentId={} userId={}",
                recordId, attachmentId, userId);
        AttachmentEntity attachment = getAttachmentEntity(recordId, attachmentId, userId);
        minioStorageService.delete(attachment.getFilePath());
        attachmentJpaRepository.delete(attachment);
        log.info("Maintenance attachment deleted recordId={} attachmentId={} userId={}",
                recordId, attachmentId, userId);
    }

    /**
     * Deletes all files currently associated with a maintenance record.
     *
     * <p>This method is intended for cascading cleanup scenarios where the parent
     * maintenance record is being removed or rebuilt.</p>
     *
     * @param recordId maintenance record identifier
     */
    @Transactional
    public void deleteAllFromRecord(String recordId) {
        log.info("Deleting all attachments from recordId={}", recordId);
        maintenanceRecordJpaRepository.findById(recordId).ifPresent(record -> {
            if (record.getAttachments() == null) {
                return;
            }
            for (AttachmentEntity attachment : record.getAttachments()) {
                minioStorageService.delete(attachment.getFilePath());
            }
        });
    }

    private AttachmentEntity getAttachmentEntity(String recordId, String attachmentId, String userId) {
        getOwnedRecord(recordId, userId);
        return getRecordAttachment(recordId, attachmentId);
    }

    private AttachmentEntity getRecordAttachment(String recordId, String attachmentId) {
        return attachmentJpaRepository.findByIdAndMaintenanceRecord_Id(attachmentId, recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Adjunto no encontrado"));
    }

    private MaintenanceRecordEntity getOwnedRecord(String recordId, String userId) {
        if (userId == null || userId.isBlank()) {
            log.warn("Maintenance attachment access rejected due to missing userId recordId={}", recordId);
            throw new BusinessValidationException("El usuario es obligatorio");
        }
        return maintenanceRecordJpaRepository.findByIdAndVehicle_User_Id(recordId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro no encontrado"));
    }

    private String generateId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
