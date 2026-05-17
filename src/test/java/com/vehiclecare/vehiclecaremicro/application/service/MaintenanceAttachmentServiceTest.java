package com.vehiclecare.vehiclecaremicro.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vehiclecare.vehiclecaremicro.application.dto.response.FileUploadResponseDTO;
import com.vehiclecare.vehiclecaremicro.domain.model.Attachment;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.AttachmentMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.AttachmentEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.MaintenanceRecordEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.UserEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.VehicleEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.AttachmentJpaRepository;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.MaintenanceRecordJpaRepository;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.BusinessValidationException;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.ResourceNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class MaintenanceAttachmentServiceTest {

    @Mock
    private MaintenanceRecordJpaRepository maintenanceRecordJpaRepository;
    @Mock
    private AttachmentJpaRepository attachmentJpaRepository;
    @Mock
    private AttachmentMapper attachmentMapper;
    @Mock
    private MinioStorageService minioStorageService;

    private MaintenanceAttachmentService service;

    @BeforeEach
    void setUp() {
        service = new MaintenanceAttachmentService(
                maintenanceRecordJpaRepository,
                attachmentJpaRepository,
                attachmentMapper,
                minioStorageService
        );
    }

    @Test
    void upload_throwsWhenFilesMissing() {
        BusinessValidationException ex = assertThrows(
                BusinessValidationException.class,
                () -> service.upload("rec-1", "user-1", null)
        );

        assertEquals("Debes adjuntar al menos un archivo", ex.getMessage());
    }

    @Test
    void upload_throwsWhenAttachmentLimitExceeded() {
        MaintenanceRecordEntity record = ownedRecord("rec-1", 1);
        MockMultipartFile file1 = file("a.pdf");
        MockMultipartFile file2 = file("b.pdf");
        when(maintenanceRecordJpaRepository.findByIdAndVehicle_User_Id("rec-1", "user-1")).thenReturn(Optional.of(record));

        BusinessValidationException ex = assertThrows(
                BusinessValidationException.class,
                () -> service.upload("rec-1", "user-1", new MockMultipartFile[] {file1, file2})
        );

        assertEquals("Solo puedes adjuntar un máximo de 2 archivos por registro", ex.getMessage());
    }

    @Test
    void upload_savesUploadedAttachments() {
        MaintenanceRecordEntity record = ownedRecord("rec-1", 0);
        MockMultipartFile file = file("a.pdf");
        FileUploadResponseDTO upload = new FileUploadResponseDTO("a.pdf", "application/pdf", 3L, "records/rec-1/a.pdf", "url");
        AttachmentEntity savedEntity = AttachmentEntity.builder()
                .id("att-1")
                .maintenanceRecord(record)
                .fileName("a.pdf")
                .fileType("application/pdf")
                .filePath("records/rec-1/a.pdf")
                .build();
        Attachment domain = new Attachment("att-1", "rec-1", "a.pdf", "application/pdf", "records/rec-1/a.pdf");
        when(maintenanceRecordJpaRepository.findByIdAndVehicle_User_Id("rec-1", "user-1")).thenReturn(Optional.of(record));
        when(minioStorageService.uploadDocument(file, "records/rec-1")).thenReturn(upload);
        when(attachmentJpaRepository.save(any(AttachmentEntity.class))).thenReturn(savedEntity);
        when(attachmentMapper.toDomain(savedEntity)).thenReturn(domain);

        List<Attachment> result = service.upload("rec-1", "user-1", new MockMultipartFile[] {file});

        assertEquals(1, result.size());
        assertSame(domain, result.get(0));
        verify(attachmentJpaRepository).save(any(AttachmentEntity.class));
    }

    @Test
    void getAttachment_returnsMappedDomain() {
        MaintenanceRecordEntity record = ownedRecord("rec-1", 0);
        AttachmentEntity entity = attachmentEntity("att-1", record);
        Attachment attachment = new Attachment("att-1", "rec-1", "a.pdf", "application/pdf", "records/rec-1/a.pdf");
        when(maintenanceRecordJpaRepository.findByIdAndVehicle_User_Id("rec-1", "user-1")).thenReturn(Optional.of(record));
        when(attachmentJpaRepository.findByIdAndMaintenanceRecord_Id("att-1", "rec-1")).thenReturn(Optional.of(entity));
        when(attachmentMapper.toDomain(entity)).thenReturn(attachment);

        Attachment result = service.getAttachment("rec-1", "att-1", "user-1");

        assertSame(attachment, result);
    }

    @Test
    void download_returnsInputStream() {
        MaintenanceRecordEntity record = ownedRecord("rec-1", 0);
        AttachmentEntity entity = attachmentEntity("att-1", record);
        InputStream stream = new ByteArrayInputStream(new byte[] {1});
        when(maintenanceRecordJpaRepository.findByIdAndVehicle_User_Id("rec-1", "user-1")).thenReturn(Optional.of(record));
        when(attachmentJpaRepository.findByIdAndMaintenanceRecord_Id("att-1", "rec-1")).thenReturn(Optional.of(entity));
        when(minioStorageService.download("records/rec-1/a.pdf")).thenReturn(stream);

        InputStream result = service.download("rec-1", "att-1", "user-1");

        assertSame(stream, result);
    }

    @Test
    void delete_removesAttachmentAndObject() {
        MaintenanceRecordEntity record = ownedRecord("rec-1", 0);
        AttachmentEntity entity = attachmentEntity("att-1", record);
        when(maintenanceRecordJpaRepository.findByIdAndVehicle_User_Id("rec-1", "user-1")).thenReturn(Optional.of(record));
        when(attachmentJpaRepository.findByIdAndMaintenanceRecord_Id("att-1", "rec-1")).thenReturn(Optional.of(entity));

        service.delete("rec-1", "att-1", "user-1");

        verify(minioStorageService).delete("records/rec-1/a.pdf");
        verify(attachmentJpaRepository).delete(entity);
    }

    @Test
    void deleteAllFromRecord_deletesEveryObjectWhenPresent() {
        MaintenanceRecordEntity record = ownedRecord("rec-1", 0);
        record.setAttachments(List.of(
                attachmentEntity("att-1", record),
                AttachmentEntity.builder().id("att-2").maintenanceRecord(record).fileName("b.pdf")
                        .fileType("application/pdf").filePath("records/rec-1/b.pdf").build()
        ));
        when(maintenanceRecordJpaRepository.findById("rec-1")).thenReturn(Optional.of(record));

        service.deleteAllFromRecord("rec-1");

        verify(minioStorageService).delete("records/rec-1/a.pdf");
        verify(minioStorageService).delete("records/rec-1/b.pdf");
    }

    @Test
    void deleteAllFromRecord_ignoresMissingRecordAndNullAttachments() {
        when(maintenanceRecordJpaRepository.findById("rec-1")).thenReturn(Optional.empty());
        service.deleteAllFromRecord("rec-1");
        verify(minioStorageService, never()).delete(any());

        MaintenanceRecordEntity record = ownedRecord("rec-2", 0);
        record.setAttachments(null);
        when(maintenanceRecordJpaRepository.findById("rec-2")).thenReturn(Optional.of(record));
        service.deleteAllFromRecord("rec-2");
        verify(minioStorageService, never()).delete(any());
    }

    @Test
    void ownedRecordValidation_throwsWhenUserMissing() {
        BusinessValidationException ex = assertThrows(
                BusinessValidationException.class,
                () -> service.download("rec-1", "att-1", " ")
        );

        assertEquals("El usuario es obligatorio", ex.getMessage());
    }

    @Test
    void ownedRecordValidation_throwsWhenRecordMissing() {
        when(maintenanceRecordJpaRepository.findByIdAndVehicle_User_Id("rec-1", "user-1")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> service.download("rec-1", "att-1", "user-1")
        );

        assertEquals("Registro no encontrado", ex.getMessage());
    }

    @Test
    void recordAttachmentValidation_throwsWhenAttachmentMissing() {
        when(maintenanceRecordJpaRepository.findByIdAndVehicle_User_Id("rec-1", "user-1")).thenReturn(Optional.of(ownedRecord("rec-1", 0)));
        when(attachmentJpaRepository.findByIdAndMaintenanceRecord_Id("att-1", "rec-1")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> service.getAttachment("rec-1", "att-1", "user-1")
        );

        assertEquals("Adjunto no encontrado", ex.getMessage());
    }

    private static MaintenanceRecordEntity ownedRecord(String recordId, int attachments) {
        UserEntity user = UserEntity.builder().id("user-1").build();
        VehicleEntity vehicle = VehicleEntity.builder().id("veh-1").user(user).build();
        MaintenanceRecordEntity record = MaintenanceRecordEntity.builder().id(recordId).vehicle(vehicle).build();
        if (attachments == 0) {
            record.setAttachments(List.of());
        } else {
            record.setAttachments(List.of(attachmentEntity("existing", record)));
        }
        return record;
    }

    private static AttachmentEntity attachmentEntity(String id, MaintenanceRecordEntity record) {
        return AttachmentEntity.builder()
                .id(id)
                .maintenanceRecord(record)
                .fileName("a.pdf")
                .fileType("application/pdf")
                .filePath("records/" + record.getId() + "/a.pdf")
                .build();
    }

    private static MockMultipartFile file(String fileName) {
        return new MockMultipartFile("file", fileName, "application/pdf", new byte[] {1, 2, 3});
    }
}
