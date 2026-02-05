package com.vehiclecare.vehiclecaremicro.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentRequestDTO {
    private String id;

    @NotBlank(message = "El nombre del archivo es obligatorio")
    @Size(max = 50, message = "El nombre del archivo no puede superar 50 caracteres")
    private String fileName;

    @NotBlank(message = "El tipo de archivo es obligatorio")
    @Pattern(
            regexp = "(?i)^(jpg|jpeg|png|pdf|doc|docx|txt)$",
            message = "El tipo de archivo no es válido"
    )
    private String fileType;

    @Size(max = 500, message = "La ruta del archivo no puede superar 500 caracteres")
    private String filePath;
}
