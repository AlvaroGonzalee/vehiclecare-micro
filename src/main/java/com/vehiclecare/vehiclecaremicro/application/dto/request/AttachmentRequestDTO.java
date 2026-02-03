package com.vehiclecare.vehiclecaremicro.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentRequestDTO {
    private String id;
    private String fileName;
    private String fileType;
    private String filePath;
}
