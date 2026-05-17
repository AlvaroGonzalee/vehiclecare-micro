package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vehiclecare.vehiclecaremicro.application.dto.request.MaintenanceRecordCreateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.request.MaintenanceRecordUpdateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.AttachmentResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.MaintenanceRecordResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.MaintenanceAttachmentService;
import com.vehiclecare.vehiclecaremicro.application.service.PublicFileUrlService;
import com.vehiclecare.vehiclecaremicro.domain.model.Attachment;
import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.port.in.AddMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.DeleteMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.GetMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.ListMaintenanceRecordsUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.UpdateMaintenanceRecordUseCase;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.AttachmentMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.MaintenanceRecordMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.security.AuthenticatedUser;
import com.vehiclecare.vehiclecaremicro.infrastructure.security.AuthenticationContext;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class MaintenanceRecordControllerTest {

    @Mock private AddMaintenanceRecordUseCase addMaintenanceRecordUseCase;
    @Mock private GetMaintenanceRecordUseCase getMaintenanceRecordUseCase;
    @Mock private ListMaintenanceRecordsUseCase listMaintenanceRecordsUseCase;
    @Mock private UpdateMaintenanceRecordUseCase updateMaintenanceRecordUseCase;
    @Mock private DeleteMaintenanceRecordUseCase deleteMaintenanceRecordUseCase;
    @Mock private MaintenanceRecordMapper maintenanceRecordMapper;
    @Mock private MaintenanceAttachmentService maintenanceAttachmentService;
    @Mock private AttachmentMapper attachmentMapper;
    @Mock private PublicFileUrlService publicFileUrlService;
    @Mock private AuthenticationContext authenticationContext;

    private MaintenanceRecordController controller;

    @BeforeEach
    void setUp() {
        controller = new MaintenanceRecordController(
                addMaintenanceRecordUseCase, getMaintenanceRecordUseCase, listMaintenanceRecordsUseCase,
                updateMaintenanceRecordUseCase, deleteMaintenanceRecordUseCase, maintenanceRecordMapper,
                maintenanceAttachmentService, attachmentMapper, publicFileUrlService, authenticationContext
        );
    }

    @Test
    void listByVehicle_returnsRecords() {
        MockHttpServletRequest request = request("user-1");
        MaintenanceRecord record = record();
        MaintenanceRecordResponseDTO dto = responseDto();
        when(listMaintenanceRecordsUseCase.listByVehicleId("veh-1", "user-1")).thenReturn(List.of(record));
        when(maintenanceRecordMapper.toResponse(record)).thenReturn(dto);
        when(publicFileUrlService.buildObjectUrl(request, "records/r1/file.pdf")).thenReturn("http://public");

        var response = controller.listByVehicle("veh-1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getRecord_returnsFoundRecord() {
        MockHttpServletRequest request = request("user-1");
        MaintenanceRecord record = record();
        MaintenanceRecordResponseDTO dto = responseDto();
        when(getMaintenanceRecordUseCase.getById("rec-1", "user-1")).thenReturn(Optional.of(record));
        when(maintenanceRecordMapper.toResponse(record)).thenReturn(dto);
        when(publicFileUrlService.buildObjectUrl(request, "records/r1/file.pdf")).thenReturn("http://public");

        var response = controller.getRecord("rec-1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getRecord_returnsNotFoundWhenMissing() {
        MockHttpServletRequest request = request("user-1");
        when(getMaintenanceRecordUseCase.getById("rec-1", "user-1")).thenReturn(Optional.empty());

        var response = controller.getRecord("rec-1", request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void addMaintenance_returnsCreated() {
        MockHttpServletRequest request = request("user-1");
        MaintenanceRecordCreateRequestDTO dto = new MaintenanceRecordCreateRequestDTO("Cambio", LocalDate.now(), "Reparación", 1000, BigDecimal.ONE, "desc", List.of());
        MaintenanceRecord record = record();
        MaintenanceRecordResponseDTO responseDto = responseDto();
        when(maintenanceRecordMapper.toDomain(dto)).thenReturn(record);
        when(addMaintenanceRecordUseCase.addMaintenanceRecord("veh-1", "user-1", record)).thenReturn(record);
        when(maintenanceRecordMapper.toResponse(record)).thenReturn(responseDto);
        when(publicFileUrlService.buildObjectUrl(request, "records/r1/file.pdf")).thenReturn("http://public");

        var response = controller.addMaintenance("veh-1", dto, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void updateRecord_returnsUpdated() {
        MockHttpServletRequest request = request("user-1");
        MaintenanceRecordUpdateRequestDTO dto = new MaintenanceRecordUpdateRequestDTO("Cambio", LocalDate.now(), "Reparación", 1000, BigDecimal.ONE, "desc", List.of());
        MaintenanceRecord record = record();
        MaintenanceRecordResponseDTO responseDto = responseDto();
        when(maintenanceRecordMapper.toDomain(dto)).thenReturn(record);
        when(updateMaintenanceRecordUseCase.update("rec-1", "user-1", record)).thenReturn(record);
        when(maintenanceRecordMapper.toResponse(record)).thenReturn(responseDto);
        when(publicFileUrlService.buildObjectUrl(request, "records/r1/file.pdf")).thenReturn("http://public");

        var response = controller.updateRecord("rec-1", dto, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteRecord_returnsNoContentWhenDeleted() {
        MockHttpServletRequest request = request("user-1");
        when(deleteMaintenanceRecordUseCase.delete("rec-1", "user-1")).thenReturn(true);

        var response = controller.deleteRecord("rec-1", request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteRecord_returnsNotFoundWhenNotDeleted() {
        MockHttpServletRequest request = request("user-1");
        when(deleteMaintenanceRecordUseCase.delete("rec-1", "user-1")).thenReturn(false);

        var response = controller.deleteRecord("rec-1", request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void uploadAttachments_returnsCreatedList() {
        MockHttpServletRequest request = request("user-1");
        MockMultipartFile file = new MockMultipartFile("files", "doc.pdf", "application/pdf", new byte[] {1});
        Attachment attachment = new Attachment("att-1", "rec-1", "doc.pdf", "application/pdf", "records/r1/file.pdf");
        AttachmentResponseDTO dto = new AttachmentResponseDTO("att-1", "doc.pdf", "application/pdf", "records/r1/file.pdf");
        when(maintenanceAttachmentService.upload("rec-1", "user-1", new MockMultipartFile[] {file})).thenReturn(List.of(attachment));
        when(attachmentMapper.toResponse(attachment)).thenReturn(dto);
        when(publicFileUrlService.buildObjectUrl(request, "records/r1/file.pdf")).thenReturn("http://public");

        var response = controller.uploadAttachments("rec-1", new MockMultipartFile[] {file}, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void uploadAttachments_allowsNullFilesArray() {
        MockHttpServletRequest request = request("user-1");
        when(maintenanceAttachmentService.upload("rec-1", "user-1", null)).thenReturn(List.of());

        var response = controller.uploadAttachments("rec-1", null, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(0, response.getBody().size());
    }

    @Test
    void downloadAttachment_returnsStream() {
        MockHttpServletRequest request = request("user-1");
        Attachment attachment = new Attachment("att-1", "rec-1", "doc.pdf", "application/pdf", "records/r1/file.pdf");
        when(maintenanceAttachmentService.getAttachment("rec-1", "att-1", "user-1")).thenReturn(attachment);
        when(maintenanceAttachmentService.download("rec-1", "att-1", "user-1")).thenReturn(new ByteArrayInputStream(new byte[] {1}));

        var response = controller.downloadAttachment("rec-1", "att-1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename*=UTF-8''doc.pdf", response.getHeaders().getFirst("Content-Disposition"));
    }

    @Test
    void downloadAttachment_usesDefaultContentTypeWhenAttachmentTypeBlank() {
        MockHttpServletRequest request = request("user-1");
        Attachment attachment = new Attachment("att-1", "rec-1", "doc.pdf", "   ", "records/r1/file.pdf");
        when(maintenanceAttachmentService.getAttachment("rec-1", "att-1", "user-1")).thenReturn(attachment);
        when(maintenanceAttachmentService.download("rec-1", "att-1", "user-1")).thenReturn(new ByteArrayInputStream(new byte[] {1}));

        var response = controller.downloadAttachment("rec-1", "att-1", request);

        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
    }

    @Test
    void downloadAttachment_usesDefaultContentTypeWhenAttachmentTypeNull() {
        MockHttpServletRequest request = request("user-1");
        Attachment attachment = new Attachment("att-1", "rec-1", "doc.pdf", null, "records/r1/file.pdf");
        when(maintenanceAttachmentService.getAttachment("rec-1", "att-1", "user-1")).thenReturn(attachment);
        when(maintenanceAttachmentService.download("rec-1", "att-1", "user-1")).thenReturn(new ByteArrayInputStream(new byte[] {1}));

        var response = controller.downloadAttachment("rec-1", "att-1", request);

        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
    }

    @Test
    void deleteAttachment_returnsNoContent() {
        MockHttpServletRequest request = request("user-1");

        var response = controller.deleteAttachment("rec-1", "att-1", request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(maintenanceAttachmentService).delete("rec-1", "att-1", "user-1");
    }

    private MockHttpServletRequest request(String userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(authenticationContext.requireCurrentUser(request)).thenReturn(new AuthenticatedUser(userId, "a@a.com"));
        return request;
    }

    private MaintenanceRecord record() {
        return new MaintenanceRecord(
                "rec-1", "veh-1", "Cambio", LocalDate.now(), "Reparación", 1000,
                BigDecimal.ONE, "desc", List.of(new Attachment("att-1", "rec-1", "doc.pdf", "application/pdf", "records/r1/file.pdf"))
        );
    }

    private MaintenanceRecordResponseDTO responseDto() {
        return new MaintenanceRecordResponseDTO(
                "rec-1", "veh-1", "Cambio", LocalDate.now(), "Reparación", 1000,
                BigDecimal.ONE, "desc", List.of(new AttachmentResponseDTO("att-1", "doc.pdf", "application/pdf", "records/r1/file.pdf"))
        );
    }
}
