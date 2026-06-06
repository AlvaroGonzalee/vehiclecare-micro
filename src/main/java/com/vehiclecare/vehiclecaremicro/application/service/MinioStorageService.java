package com.vehiclecare.vehiclecaremicro.application.service;

import com.vehiclecare.vehiclecaremicro.application.dto.response.FileUploadResponseDTO;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.BusinessValidationException;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Encapsulates file storage operations backed by MinIO.
 *
 * <p>This service validates uploaded content, generates sanitized object keys,
 * initializes the target bucket when configured to do so and exposes a small API
 * for uploading, downloading and deleting files. It is used by both vehicle image
 * and maintenance attachment workflows.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    private static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/jpeg",
            "image/png"
    );
    private static final Set<String> ALLOWED_DOCUMENT_EXTENSIONS = Set.of(
            ".pdf",
            ".doc",
            ".docx",
            ".jpeg",
            ".jpg",
            ".png"
    );
    private static final long MAX_FILE_SIZE_BYTES = 500L * 1024 * 1024;
    private static final long MAX_DOCUMENT_SIZE_BYTES = 10L * 1024 * 1024;

    private final MinioClient minioClient;

    @Value("${minio.bucket:vehiclecare}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.initialize-bucket:false}")
    private boolean initializeBucketOnStartup;

    /**
     * Ensures that the configured bucket exists when startup initialization is enabled.
     */
    @PostConstruct
    public void initializeBucket() {
        if (!initializeBucketOnStartup) {
            log.info("MinIO bucket initialization skipped bucket={}", bucketName);
            return;
        }
        try {
            log.info("Checking MinIO bucket existence bucket={}", bucketName);
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                log.info("Creating MinIO bucket bucket={}", bucketName);
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
            }
            log.info("MinIO bucket ready bucket={}", bucketName);
        } catch (Exception ex) {
            log.error("Failed to initialize MinIO bucket bucket={}", bucketName, ex);
            throw new IllegalStateException("No se pudo inicializar el bucket de MinIO", ex);
        }
    }

    /**
     * Uploads an image after validating content type and file size constraints.
     *
     * @param file multipart image file
     * @param folder logical folder prefix inside the bucket
     * @return metadata about the stored object
     */
    public FileUploadResponseDTO uploadImage(MultipartFile file, String folder) {
        log.info("Uploading image to MinIO folder={} originalFileName={} size={} contentType={}",
                folder, file.getOriginalFilename(), file.getSize(), file.getContentType());
        validateImage(file);
        String objectKey = upload(file, folder, getExtension(file.getOriginalFilename(), file.getContentType()));

        String objectUrl = buildObjectUrl(objectKey);
        return new FileUploadResponseDTO(
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                objectKey,
                objectUrl
        );
    }

    /**
     * Uploads a document or image attachment allowed by the maintenance module.
     *
     * @param file multipart document file
     * @param folder logical folder prefix inside the bucket
     * @return metadata about the stored object
     */
    public FileUploadResponseDTO uploadDocument(MultipartFile file, String folder) {
        log.info("Uploading document to MinIO folder={} originalFileName={} size={} contentType={}",
                folder, file.getOriginalFilename(), file.getSize(), file.getContentType());
        validateDocument(file);
        String extension = getExtension(file.getOriginalFilename(), file.getContentType());
        String contentType = resolveDocumentContentType(file.getContentType(), extension);
        String objectKey = upload(file, folder, extension, contentType);
        return new FileUploadResponseDTO(
                file.getOriginalFilename(),
                contentType,
                file.getSize(),
                objectKey,
                buildObjectUrl(objectKey)
        );
    }

    /**
     * Downloads an object from MinIO.
     *
     * @param objectKey storage object key
     * @return input stream for the object content
     */
    public InputStream download(String objectKey) {
        try {
            log.info("Downloading object from MinIO objectKey={}", objectKey);
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception ex) {
            log.error("Failed to download object from MinIO objectKey={}", objectKey, ex);
            throw new BusinessValidationException("No se pudo descargar el archivo");
        }
    }

    /**
     * Deletes an object from MinIO when the key is present.
     *
     * @param objectKey storage object key
     */
    public void delete(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            log.warn("Skipping MinIO delete because objectKey is blank");
            return;
        }
        try {
            log.info("Deleting object from MinIO objectKey={}", objectKey);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception ex) {
            log.error("Failed to delete object from MinIO objectKey={}", objectKey, ex);
            throw new BusinessValidationException("No se pudo borrar el archivo");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessValidationException("El archivo es obligatorio");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessValidationException("La imagen supera el límite de 500MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new BusinessValidationException("Formato no permitido. Usa JPG, PNG o WEBP");
        }
    }

    private void validateDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessValidationException("El archivo es obligatorio");
        }
        if (file.getSize() > MAX_DOCUMENT_SIZE_BYTES) {
            throw new BusinessValidationException("El archivo supera el límite de 10MB");
        }
        String extension = getExtension(file.getOriginalFilename(), file.getContentType());
        String contentType = file.getContentType();
        if (!ALLOWED_DOCUMENT_EXTENSIONS.contains(extension)) {
            throw new BusinessValidationException("Formato no permitido. Usa PDF, DOC, DOCX, JPG o PNG");
        }
        if (contentType != null
                && !"application/octet-stream".equals(contentType)
                && !ALLOWED_DOCUMENT_TYPES.contains(contentType)) {
            throw new BusinessValidationException("Formato no permitido. Usa PDF, DOC, DOCX, JPG o PNG");
        }
    }

    private String upload(MultipartFile file, String folder, String extension) {
        return upload(file, folder, extension, file.getContentType());
    }

    private String upload(
            MultipartFile file,
            String folder,
            String extension,
            String contentType
    ) {
        String safeFolder = sanitizeFolder(folder);
        String objectKey = safeFolder + "/" + UUID.randomUUID() + extension;
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
            log.info("Object uploaded to MinIO objectKey={} bucket={}", objectKey, bucketName);
            return objectKey;
        } catch (MinioException | IOException ex) {
            log.error("MinIO upload failed objectKey={} bucket={}", objectKey, bucketName, ex);
            throw new BusinessValidationException("No se pudo subir el archivo a MinIO");
        } catch (Exception ex) {
            log.error("Unexpected MinIO upload error objectKey={} bucket={}", objectKey, bucketName, ex);
            throw new BusinessValidationException("Error inesperado al subir el archivo");
        }
    }

    private String sanitizeFolder(String folder) {
        if (folder == null || folder.isBlank()) {
            return "uploads";
        }
        return folder.toLowerCase().replaceAll("[^a-z0-9/_-]", "");
    }

    private String getExtension(String fileName, String contentType) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        }
        if ("image/png".equals(contentType)) {
            return ".png";
        }
        if ("image/webp".equals(contentType)) {
            return ".webp";
        }
        if ("application/pdf".equals(contentType)) {
            return ".pdf";
        }
        if ("application/msword".equals(contentType)) {
            return ".doc";
        }
        if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(contentType)) {
            return ".docx";
        }
        return ".jpg";
    }

    private String buildObjectUrl(String objectKey) {
        return endpoint + "/" + bucketName + "/" + objectKey;
    }

    private String resolveDocumentContentType(String contentType, String extension) {
        if (contentType != null && !"application/octet-stream".equals(contentType)) {
            return contentType;
        }
        return switch (extension) {
            case ".pdf" -> "application/pdf";
            case ".doc" -> "application/msword";
            case ".docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case ".png" -> "image/png";
            case ".jpeg", ".jpg" -> "image/jpeg";
            default -> "application/octet-stream";
        };
    }
}
