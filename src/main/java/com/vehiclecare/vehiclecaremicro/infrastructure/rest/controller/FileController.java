package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.dto.response.FileUploadResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.MinioStorageService;
import com.vehiclecare.vehiclecaremicro.application.service.PublicFileUrlService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final MinioStorageService minioStorageService;
    private final PublicFileUrlService publicFileUrlService;

    @PostMapping("/upload-image")
    public ResponseEntity<FileUploadResponseDTO> uploadImage(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "uploads") String folder
    ) {
        FileUploadResponseDTO response = minioStorageService.uploadImage(file, folder);
        response.setObjectUrl(publicFileUrlService.buildObjectUrl(request, response.getObjectKey()));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/object")
    public ResponseEntity<InputStreamResource> getObject(@RequestParam("key") String key) {
        String objectKey = publicFileUrlService.extractObjectKey(key);
        InputStream stream = minioStorageService.download(objectKey);
        MediaType mediaType = MediaTypeFactory.getMediaType(objectKey)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(new InputStreamResource(stream));
    }
}
