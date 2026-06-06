package com.vehiclecare.vehiclecaremicro.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain representation of a vehicle owned by a user.
 *
 * <p>A {@code Vehicle} contains the identifying and descriptive data required by the
 * business layer to manage a user's fleet, including ownership, registration details,
 * technical attributes and the current odometer reading. The model is persistence-agnostic
 * and is shared across use cases, controllers and repository ports.</p>
 */
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
    private String imageUrl;
}
