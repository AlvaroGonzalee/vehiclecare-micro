package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vehiclecare.vehiclecaremicro.application.dto.response.FileUploadResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.MinioStorageService;
import com.vehiclecare.vehiclecaremicro.application.service.PublicFileUrlService;
import java.io.ByteArrayInputStream;
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
class FileControllerTest {

    @Mock
    private MinioStorageService minioStorageService;
    @Mock
    private PublicFileUrlService publicFileUrlService;

    private FileController controller;

    @BeforeEach
    void setUp() {
        controller = new FileController(minioStorageService, publicFileUrlService);
    }

    @Test
    void uploadImage_returnsCreatedWithPublicUrl() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[] {1});
        FileUploadResponseDTO upload = new FileUploadResponseDTO("image.jpg", "image/jpeg", 1L, "uploads/image.jpg", "internal");
        when(minioStorageService.uploadImage(file, "uploads")).thenReturn(upload);
        when(publicFileUrlService.buildObjectUrl(request, "uploads/image.jpg")).thenReturn("http://public");

        var response = controller.uploadImage(request, file, "uploads");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("http://public", response.getBody().getObjectUrl());
    }

    @Test
    void getObject_returnsDetectedMediaType() {
        when(publicFileUrlService.extractObjectKey("uploads/file.pdf")).thenReturn("uploads/file.pdf");
        when(minioStorageService.download("uploads/file.pdf")).thenReturn(new ByteArrayInputStream(new byte[] {1}));

        var response = controller.getObject("uploads/file.pdf");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        verify(minioStorageService).download("uploads/file.pdf");
    }

    @Test
    void getObject_fallsBackToOctetStreamWhenTypeUnknown() {
        when(publicFileUrlService.extractObjectKey("uploads/file.unknown")).thenReturn("uploads/file.unknown");
        when(minioStorageService.download("uploads/file.unknown")).thenReturn(new ByteArrayInputStream(new byte[] {1}));

        var response = controller.getObject("uploads/file.unknown");

        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
    }
}
