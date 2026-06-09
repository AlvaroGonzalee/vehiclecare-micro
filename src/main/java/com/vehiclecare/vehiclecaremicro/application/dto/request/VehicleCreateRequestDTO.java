package com.vehiclecare.vehiclecaremicro.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCreateRequestDTO {
    @NotBlank(message = "El usuario es obligatorio")
    private String userId;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 100, message = "La marca no puede superar 100 caracteres")
    private String brand;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 100, message = "El modelo no puede superar 100 caracteres")
    private String model;

    @NotNull(message = "El año es obligatorio")
    private Integer year;

    @NotBlank(message = "La matrícula es obligatoria")
    @Pattern(
            regexp = "^[0-9]{4}\\s?[B-DF-HJ-NP-TV-Zb-df-hj-np-tv-z]{3}$",
            message = "La matrícula debe tener formato español 1234 BCD, sin vocales."
    )
    private String licensePlate;

    @Pattern(
            regexp = "^$|^[A-HJ-NPR-Z0-9]{17}$",
            message = "El VIN debe tener 17 caracteres alfanuméricos válidos"
    )
    private String vin;

    @PositiveOrZero(message = "Los kilómetros actuales no pueden ser negativos")
    private Integer currentKilometers;

    @Pattern(
            regexp = "^(Gasolina|Diésel|Híbrido|Eléctrico)$",
            message = "El tipo de combustible debe ser Gasolina, Diésel, Híbrido o Eléctrico"
    )
    private String fuelType;
}
