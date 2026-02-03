package com.vehiclecare.vehiclecaremicro.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Vehicle {
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
