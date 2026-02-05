package com.vehiclecare.vehiclecaremicro.application.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRecordCreateRequestDTO {
    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150, message = "El título no puede superar 150 caracteres")
    private String title;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate date;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 100, message = "La categoría no puede superar 100 caracteres")
    @Pattern(
            regexp = "^(?i)(Reparación|Mantenimiento|Mejora|Extra|Extras)$",
            message = "La categoría debe ser: Reparación, Mantenimiento, Mejora o Extra"
    )
    private String category;

    private Integer kilometers;

    private BigDecimal price;

    private String description;

    @Valid
    private List<AttachmentRequestDTO> attachments;
}
