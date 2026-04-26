package com.vehiclecare.vehiclecaremicro.application.service;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class PublicFileUrlService {

    private final String bucketName;

    public PublicFileUrlService(@Value("${minio.bucket:vehiclecare}") String bucketName) {
        this.bucketName = bucketName;
    }

    @Nullable
    public String buildObjectUrl(HttpServletRequest request, @Nullable String storedReference) {
        String objectKey = extractObjectKey(storedReference);
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath("/files/object")
                .replaceQuery(null)
                .queryParam("key", objectKey)
                .build()
                .toUriString();
    }

    @Nullable
    public String extractObjectKey(@Nullable String storedReference) {
        if (storedReference == null || storedReference.isBlank()) {
            return null;
        }

        URI uri = URI.create(storedReference);
        if (uri.getScheme() == null || uri.getHost() == null) {
            return normalizeKey(storedReference);
        }

        String path = uri.getPath();
        if (path == null || path.isBlank()) {
            return null;
        }

        String normalizedPath = normalizeKey(path);
        String bucketPrefix = bucketName + "/";
        if (normalizedPath.startsWith(bucketPrefix)) {
          return normalizedPath.substring(bucketPrefix.length());
        }
        return normalizedPath;
    }

    private String normalizeKey(String value) {
        String normalized = value.strip();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}
