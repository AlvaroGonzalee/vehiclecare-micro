package com.vehiclecare.vehiclecaremicro.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vehiclecare.vehiclecaremicro.application.dto.response.FileUploadResponseDTO;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.BusinessValidationException;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class MinioStorageServiceTest {

    @Mock
    private MinioClient minioClient;

    private MinioStorageService service;

    @BeforeEach
    void setUp() {
        service = new MinioStorageService(minioClient);
        ReflectionTestUtils.setField(service, "bucketName", "vehiclecare");
        ReflectionTestUtils.setField(service, "endpoint", "http://localhost:9000");
        ReflectionTestUtils.setField(service, "initializeBucketOnStartup", false);
    }

    @Test
    void initializeBucket_skipsWhenDisabled() throws Exception {
        service.initializeBucket();

        verify(minioClient, never()).bucketExists(any());
    }

    @Test
    void initializeBucket_createsBucketWhenMissing() throws Exception {
        ReflectionTestUtils.setField(service, "initializeBucketOnStartup", true);
        when(minioClient.bucketExists(any())).thenReturn(false);

        service.initializeBucket();

        verify(minioClient).bucketExists(any());
        verify(minioClient).makeBucket(any());
    }

    @Test
    void initializeBucket_doesNothingWhenBucketExists() throws Exception {
        ReflectionTestUtils.setField(service, "initializeBucketOnStartup", true);
        when(minioClient.bucketExists(any())).thenReturn(true);

        service.initializeBucket();

        verify(minioClient).bucketExists(any());
        verify(minioClient, never()).makeBucket(any());
    }

    @Test
    void initializeBucket_throwsWhenMinioFails() throws Exception {
        ReflectionTestUtils.setField(service, "initializeBucketOnStartup", true);
        when(minioClient.bucketExists(any())).thenThrow(new RuntimeException("boom"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.initializeBucket());

        assertEquals("No se pudo inicializar el bucket de MinIO", ex.getMessage());
    }

    @Test
    void uploadImage_uploadsAndBuildsResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "photo.JPG", "image/jpeg", new byte[] {1, 2});

        FileUploadResponseDTO response = service.uploadImage(file, "Vehicle Images/ABC");

        assertEquals("photo.JPG", response.getFileName());
        assertEquals("image/jpeg", response.getContentType());
        assertEquals(2L, response.getSize());
        assertNotNull(response.getObjectKey());
        assertTrueStartsWith(response.getObjectKey(), "vehicleimages/abc/");
        assertTrueStartsWith(response.getObjectUrl(), "http://localhost:9000/vehiclecare/");
        verify(minioClient).putObject(any());
    }

    @Test
    void uploadImage_validatesEmptyLargeAndInvalidType() throws Exception {
        assertBusinessMessage("El archivo es obligatorio", () -> service.uploadImage(new MockMultipartFile("file", new byte[0]), "x"));

        MultipartFile large = multipart("big.jpg", "image/jpeg", 600L * 1024 * 1024, false, null);
        assertBusinessMessage("La imagen supera el límite de 500MB", () -> service.uploadImage(large, "x"));

        MultipartFile invalid = multipart("file.gif", "image/gif", 1L, false, null);
        assertBusinessMessage("Formato no permitido. Usa JPG, PNG o WEBP", () -> service.uploadImage(invalid, "x"));
    }

    @Test
    void uploadDocument_uploadsAndResolvesOctetStreamContentType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "report.docx", "application/octet-stream", new byte[] {1});

        FileUploadResponseDTO response = service.uploadDocument(file, "records/REC-1");

        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", response.getContentType());
        assertTrueStartsWith(response.getObjectKey(), "records/rec-1/");
        verify(minioClient).putObject(any());
    }

    @Test
    void uploadDocument_allowsPngByContentTypeFallbackAndDefaultFolder() throws Exception {
        MultipartFile file = multipart(null, "image/png", 2L, false, new ByteArrayInputStream(new byte[] {1, 2}));

        FileUploadResponseDTO response = service.uploadDocument(file, "   ");

        assertEquals("image/png", response.getContentType());
        assertTrueStartsWith(response.getObjectKey(), "uploads/");
    }

    @Test
    void uploadDocument_validatesEmptyLargeInvalidExtensionAndInvalidContentType()
        throws IOException {
        assertBusinessMessage("El archivo es obligatorio", () -> service.uploadDocument(new MockMultipartFile("file", new byte[0]), "x"));

        MultipartFile large = multipart("big.pdf", "application/pdf", 11L * 1024 * 1024, false, null);
        assertBusinessMessage("El archivo supera el límite de 10MB", () -> service.uploadDocument(large, "x"));

        MultipartFile badExtension = multipart("malware.exe", "application/octet-stream", 1L, false, null);
        assertBusinessMessage("Formato no permitido. Usa PDF, DOC, DOCX, JPG o PNG", () -> service.uploadDocument(badExtension, "x"));

        MultipartFile badType = multipart("image.pdf", "text/plain", 1L, false, null);
        assertBusinessMessage("Formato no permitido. Usa PDF, DOC, DOCX, JPG o PNG", () -> service.uploadDocument(badType, "x"));
    }

    @Test
    void uploadDocument_coversExtensionFallbacks() throws Exception {
        Map<String, String> expected = Map.of(
                "image/webp", "image/webp",
                "application/pdf", "application/pdf",
                "application/msword", "application/msword",
                "image/jpeg", "image/jpeg"
        );

        for (Map.Entry<String, String> entry : expected.entrySet()) {
            MultipartFile file = multipart(null, entry.getKey(), 1L, false, new ByteArrayInputStream(new byte[] {1}));
            FileUploadResponseDTO response = entry.getKey().equals("image/webp")
                    ? service.uploadImage(file, "x")
                    : service.uploadDocument(file, "x");
            assertEquals(entry.getValue(), response.getContentType());
        }
    }

    @Test
    void upload_wrapsIoAndUnexpectedErrors() throws Exception {
        MultipartFile ioFile = multipart("a.pdf", "application/pdf", 1L, false, null);
        when(ioFile.getInputStream()).thenThrow(new IOException("io"));
        assertBusinessMessage("No se pudo subir el archivo a MinIO", () -> service.uploadDocument(ioFile, "x"));

        MultipartFile runtimeFile = multipart("a.pdf", "application/pdf", 1L, false, new ByteArrayInputStream(new byte[] {1}));
        when(minioClient.putObject(any())).thenThrow(new RuntimeException("boom"));
        assertBusinessMessage("Error inesperado al subir el archivo", () -> service.uploadDocument(runtimeFile, "x"));
    }

    @Test
    void download_returnsStreamAndWrapsErrors() throws Exception {
        GetObjectResponse stream = org.mockito.Mockito.mock(GetObjectResponse.class);
        when(minioClient.getObject(any())).thenReturn(stream);

        InputStream result = service.download("records/a.pdf");

        assertSame(stream, result);

        when(minioClient.getObject(any())).thenThrow(new RuntimeException("boom"));
        assertBusinessMessage("No se pudo descargar el archivo", () -> service.download("records/a.pdf"));
    }

    @Test
    void delete_skipsBlankAndWrapsErrors() throws Exception {
        service.delete(" ");
        verify(minioClient, never()).removeObject(any());

        service.delete("records/a.pdf");
        verify(minioClient).removeObject(any());

        doThrow(new RuntimeException("boom")).when(minioClient).removeObject(any());
        assertBusinessMessage("No se pudo borrar el archivo", () -> service.delete("records/a.pdf"));
    }

    private static MultipartFile multipart(
            String fileName,
            String contentType,
            long size,
            boolean empty,
            InputStream stream
    ) throws IOException {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(fileName);
        when(file.getContentType()).thenReturn(contentType);
        when(file.getSize()).thenReturn(size);
        when(file.isEmpty()).thenReturn(empty);
        if (stream != null) {
            when(file.getInputStream()).thenReturn(stream);
        }
        return file;
    }

    private static void assertBusinessMessage(String expected, Runnable action) {
        BusinessValidationException ex = assertThrows(BusinessValidationException.class, action::run);
        assertEquals(expected, ex.getMessage());
    }

    private static void assertTrueStartsWith(String value, String prefix) {
        org.junit.jupiter.api.Assertions.assertTrue(value.startsWith(prefix), value);
    }
}
