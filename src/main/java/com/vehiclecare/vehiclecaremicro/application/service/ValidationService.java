package com.vehiclecare.vehiclecaremicro.application.service;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.model.User;
import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import java.time.LocalDate;
import java.time.Year;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * Centralizes normalization and business validation for domain objects before persistence.
 *
 * <p>This service applies a consistent set of rules to user, vehicle and maintenance
 * data. Besides validating mandatory fields and value ranges, it also normalizes
 * input such as trimming whitespace, formatting license plates and lower-casing emails.
 * The goal is to keep use cases lean while enforcing the same invariants across
 * every write operation.</p>
 */
@Service
public class ValidationService {

    private static final int MIN_YEAR = 1900;
    private static final int MAX_NAME_LENGTH = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final String LICENSE_PLATE_PATTERN = "^[0-9]{4} [B-DF-HJ-NP-TV-Z]{3}$";
    private static final String VIN_PATTERN = "^[A-HJ-NPR-Z0-9]{17}$";

    /**
     * Normalizes and validates user data before it is processed by a use case.
     *
     * @param user user to normalize and validate
     * @throws IllegalArgumentException if the user data violates business rules
     */
    public void normalizeAndValidateUser(User user) {
        user.setName(normalizeName(user.getName()));
        user.setEmail(normalizeEmail(user.getEmail()));
    }

    /**
     * Normalizes and validates vehicle data before creation or update.
     *
     * @param vehicle vehicle to normalize and validate
     * @throws IllegalArgumentException if the vehicle data violates business rules
     */
    public void normalizeAndValidateVehicle(Vehicle vehicle) {
        vehicle.setBrand(trimToNull(vehicle.getBrand()));
        vehicle.setModel(trimToNull(vehicle.getModel()));
        vehicle.setLicensePlate(normalizeLicensePlate(vehicle.getLicensePlate()));
        vehicle.setVin(normalizeVin(vehicle.getVin()));
        vehicle.setFuelType(normalizeFuelType(vehicle.getFuelType()));

        validateVehicleYear(vehicle.getYear());
        validateKilometers(vehicle.getCurrentKilometers(), "Los kilómetros actuales no pueden ser negativos");
    }

    /**
     * Normalizes and validates maintenance record data before persistence.
     *
     * @param maintenanceRecord maintenance record to normalize and validate
     * @throws IllegalArgumentException if the maintenance data violates business rules
     */
    public void normalizeAndValidateMaintenance(MaintenanceRecord maintenanceRecord) {
        maintenanceRecord.setTitle(trimToNull(maintenanceRecord.getTitle()));
        maintenanceRecord.setCategory(normalizeCategory(maintenanceRecord.getCategory()));
        maintenanceRecord.setDescription(normalizeDescription(maintenanceRecord.getDescription()));

        if (maintenanceRecord.getDate() != null && maintenanceRecord.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha del mantenimiento no puede estar en el futuro");
        }

        validateKilometers(maintenanceRecord.getKilometers(), "Los kilómetros no pueden ser negativos");

        if (maintenanceRecord.getPrice() != null && maintenanceRecord.getPrice().signum() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
    }

    private String normalizeName(String name) {
        String value = trimToNull(name);
        if (value == null) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (value.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("El nombre no puede superar 10 caracteres");
        }
        if (!value.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
            throw new IllegalArgumentException("El nombre solo puede contener letras y espacios");
        }
        return value;
    }

    private String normalizeEmail(String email) {
        String value = trimToNull(email);
        if (value == null) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private String normalizeLicensePlate(String licensePlate) {
        String value = trimToNull(licensePlate);
        if (value == null) {
            throw new IllegalArgumentException("La matrícula es obligatoria");
        }

        String compact = value.toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
        if (!compact.matches("^[0-9]{4}[B-DF-HJ-NP-TV-Z]{3}$")) {
            throw new IllegalArgumentException("La matrícula debe tener formato español 1234 ABC");
        }
        return compact.substring(0, 4) + " " + compact.substring(4);
    }

    private String normalizeVin(String vin) {
        String value = trimToNull(vin);
        if (value == null) {
            return null;
        }
        String normalized = value.toUpperCase(Locale.ROOT);
        if (!normalized.matches(VIN_PATTERN)) {
            throw new IllegalArgumentException("El VIN debe tener 17 caracteres alfanuméricos válidos");
        }
        return normalized;
    }

    private String normalizeFuelType(String fuelType) {
        String value = trimToNull(fuelType);
        if (value == null) {
            return null;
        }
        if (!value.matches("^(Gasolina|Diésel|Híbrido|Eléctrico)$")) {
            throw new IllegalArgumentException("El tipo de combustible debe ser Gasolina, Diésel, Híbrido o Eléctrico");
        }
        return value;
    }

    private String normalizeCategory(String category) {
        String value = trimToNull(category);
        if (value == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        return value;
    }

    private String normalizeDescription(String description) {
        String value = trimToNull(description);
        if (value == null) {
            return null;
        }
        if (value.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("La descripción no puede superar 1000 caracteres");
        }
        return value;
    }

    private void validateVehicleYear(Integer year) {
        if (year == null) {
            throw new IllegalArgumentException("El año es obligatorio");
        }
        int maxYear = Year.now().getValue() + 1;
        if (year < MIN_YEAR || year > maxYear) {
            throw new IllegalArgumentException("El año debe estar entre 1900 y " + maxYear);
        }
    }

    private void validateKilometers(Integer value, String message) {
        if (value != null && value < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
