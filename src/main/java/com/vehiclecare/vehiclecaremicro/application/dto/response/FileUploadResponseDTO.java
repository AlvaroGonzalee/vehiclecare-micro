package com.vehiclecare.vehiclecaremicro.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUploadResponseDTO {
    private String fileName;
    private String contentType;
    private long size;
    private String objectKey;
    private String objectUrl;
}
