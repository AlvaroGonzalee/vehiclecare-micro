package com.vehiclecare.vehiclecaremicro.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleUpdateRequestDTO {
    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 100, message = "La marca no puede superar 100 caracteres")
    private String brand;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 100, message = "El modelo no puede superar 100 caracteres")
    private String model;

    @NotNull(message = "El año es obligatorio")
    private Integer year;

    @NotBlank(message = "La matrícula es obligatoria")
    @Size(min = 7, max = 7, message = "La matrícula debe tener 7 caracteres")
    private String licensePlate;

    @Size(max = 50, message = "El VIN no puede superar 50 caracteres")
    private String vin;

    private Integer currentKilometers;

    @Size(max = 50, message = "El tipo de combustible no puede superar 50 caracteres")
    private String fuelType;
}
