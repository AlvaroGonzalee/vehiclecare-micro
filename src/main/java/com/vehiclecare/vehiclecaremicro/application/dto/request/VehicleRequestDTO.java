package com.vehiclecare.vehiclecaremicro.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequestDTO {
    private String id;
    private String userId;
    private String brand;
    private String model;
    private Integer year;
    private String licensePlate;
    private String vin;
    private Integer currentKilometers;
    private String fuelType;
}
