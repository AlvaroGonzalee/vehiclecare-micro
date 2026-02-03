package com.vehiclecare.vehiclecaremicro.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponseDTO {
    private String id;
    private String fileName;
    private String fileType;
    private String filePath;
}
