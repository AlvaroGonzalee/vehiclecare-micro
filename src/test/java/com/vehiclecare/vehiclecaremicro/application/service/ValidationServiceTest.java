package com.vehiclecare.vehiclecaremicro.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.model.User;
import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidationServiceTest {

    private ValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new ValidationService();
    }

    @Test
    void normalizeAndValidateUser_normalizesNameAndEmail() {
        User user = new User();
        user.setName("  Alvaro  ");
        user.setEmail("  TEST@MAIL.COM  ");

        validationService.normalizeAndValidateUser(user);

        assertEquals("Alvaro", user.getName());
        assertEquals("test@mail.com", user.getEmail());
    }

    @Test
    void normalizeAndValidateUser_throwsWhenNameIsMissing() {
        User user = new User();
        user.setName("   ");
        user.setEmail("test@mail.com");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.normalizeAndValidateUser(user)
        );

        assertEquals("El nombre es obligatorio", ex.getMessage());
    }

    @Test
    void normalizeAndValidateUser_throwsWhenNameIsTooLong() {
        User user = new User();
        user.setName("DemasiadoLargo");
        user.setEmail("test@mail.com");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.normalizeAndValidateUser(user)
        );

        assertEquals("El nombre no puede superar 10 caracteres", ex.getMessage());
    }

    @Test
    void normalizeAndValidateUser_throwsWhenNameContainsInvalidCharacters() {
        User user = new User();
        user.setName("Alvaro123");
        user.setEmail("test@mail.com");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.normalizeAndValidateUser(user)
        );

        assertEquals("El nombre solo puede contener letras y espacios", ex.getMessage());
    }

    @Test
    void normalizeAndValidateUser_throwsWhenEmailIsMissing() {
        User user = new User();
        user.setName("Alvaro");
        user.setEmail("   ");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.normalizeAndValidateUser(user)
        );

        assertEquals("El email es obligatorio", ex.getMessage());
    }

    @Test
    void normalizeAndValidateVehicle_normalizesFields() {
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("  BMW  ");
        vehicle.setModel(" 320i ");
        vehicle.setLicensePlate("1234BCD");
        vehicle.setVin("wba12345678901234");
        vehicle.setFuelType("Gasolina");
        vehicle.setYear(2020);
        vehicle.setCurrentKilometers(120000);

        validationService.normalizeAndValidateVehicle(vehicle);

        assertEquals("BMW", vehicle.getBrand());
        assertEquals("320i", vehicle.getModel());
        assertEquals("1234 BCD", vehicle.getLicensePlate());
        assertEquals("WBA12345678901234", vehicle.getVin());
        assertEquals("Gasolina", vehicle.getFuelType());
    }

    @Test
    void normalizeAndValidateVehicle_allowsNullOptionalFields() {
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("BMW");
        vehicle.setModel("320i");
        vehicle.setLicensePlate("1234BCD");
        vehicle.setYear(2020);
        vehicle.setVin("   ");
        vehicle.setFuelType("   ");

        validationService.normalizeAndValidateVehicle(vehicle);

        assertNull(vehicle.getVin());
        assertNull(vehicle.getFuelType());
    }

    @Test
    void normalizeAndValidateVehicle_throwsWhenLicensePlateIsInvalid() {
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("BMW");
        vehicle.setModel("320i");
        vehicle.setLicensePlate("BAD");
        vehicle.setYear(2020);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.normalizeAndValidateVehicle(vehicle)
        );

        assertEquals("La matrícula debe tener formato español 1234 BCD, sin vocales.", ex.getMessage());
    }

    @Test
    void normalizeAndValidateVehicle_throwsWhenLicensePlateIsMissing() {
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("BMW");
        vehicle.setModel("320i");
        vehicle.setLicensePlate("   ");
        vehicle.setYear(2020);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.normalizeAndValidateVehicle(vehicle)
        );

        assertEquals("La matrícula es obligatoria", ex.getMessage());
    }

    @Test
    void normalizeAndValidateVehicle_throwsWhenVinIsInvalid() {
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("BMW");
        vehicle.setModel("320i");
        vehicle.setLicensePlate("1234BCD");
        vehicle.setYear(2020);
        vehicle.setVin("INVALIDVIN");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.normalizeAndValidateVehicle(vehicle)
        );

        assertEquals("El VIN debe tener 17 caracteres alfanuméricos válidos", ex.getMessage());
    }

    @Test
    void normalizeAndValidateVehicle_throwsWhenFuelTypeIsInvalid() {
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("BMW");
        vehicle.setModel("320i");
        vehicle.setLicensePlate("1234BCD");
        vehicle.setYear(2020);
        vehicle.setFuelType("Agua");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.normalizeAndValidateVehicle(vehicle)
        );

        assertEquals("El tipo de combustible debe ser Gasolina, Diésel, Híbrido o Eléctrico", ex.getMessage());
    }

    @Test
    void normalizeAndValidateVehicle_throwsWhenYearIsMissing() {
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("BMW");
        vehicle.setModel("320i");
        vehicle.setLicensePlate("1234BCD");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.normalizeAndValidateVehicle(vehicle)
        );

        assertEquals("El año es obligatorio", ex.getMessage());
    }

    @Test
    void normalizeAndValidateVehicle_throwsWhenYearIsOutOfRange() {
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("BMW");
        vehicle.setModel("320i");
        vehicle.setLicensePlate("1234BCD");
        vehicle.setYear(1800);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.normalizeAndValidateVehicle(vehicle)
        );

        assertEquals("El año debe estar entre 1900 y " + (LocalDate.now().getYear() + 1) + ".", ex.getMessage());
    }

    @Test
    void normalizeAndValidateVehicle_throwsWhenYearIsAboveMaximum() {
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("BMW");
        vehicle.setModel("320i");
        vehicle.setLicensePlate("1234BCD");
        vehicle.setYear(LocalDate.now().getYear() + 2);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.normalizeAndValidateVehicle(vehicle)
        );

        assertEquals("El año debe estar entre 1900 y " + (LocalDate.now().getYear() + 1) + ".", ex.getMessage());
    }

    @Test
    void normalizeAndValidateVehicle_throwsWhenKilometersAreNegative() {
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("BMW");
        vehicle.setModel("320i");
        vehicle.setLicensePlate("1234BCD");
        vehicle.setYear(2020);
        vehicle.setCurrentKilometers(-1);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.normalizeAndValidateVehicle(vehicle)
        );

        assertEquals("Los kilómetros actuales no pueden ser negativos", ex.getMessage());
    }

    @Test
    void normalizeAndValidateMaintenance_normalizesFields() {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setTitle(" Cambio de aceite ");
        record.setCategory(" Reparación ");
        record.setDescription(" Descripción ");
        record.setDate(LocalDate.now());
        record.setKilometers(1000);
        record.setPrice(BigDecimal.ONE);

        validationService.normalizeAndValidateMaintenance(record);

        assertEquals("Cambio de aceite", record.getTitle());
        assertEquals("Reparación", record.getCategory());
        assertEquals("Descripción", record.getDescription());
    }

    @Test
    void normalizeAndValidateMaintenance_allowsNullDescription() {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setTitle("Cambio");
        record.setCategory("Reparación");
        record.setDescription("   ");
        record.setDate(LocalDate.now());

        validationService.normalizeAndValidateMaintenance(record);

        assertNull(record.getDescription());
    }

    @Test
    void normalizeAndValidateMaintenance_allowsNullDate() {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setTitle("Cambio");
        record.setCategory("Reparación");
        record.setDate(null);

        validationService.normalizeAndValidateMaintenance(record);

        assertNull(record.getDate());
    }

    @Test
    void normalizeAndValidateMaintenance_throwsWhenCategoryMissing() {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setTitle("Cambio");
        record.setCategory("  ");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.normalizeAndValidateMaintenance(record)
        );

        assertEquals("La categoría es obligatoria", ex.getMessage());
    }

    @Test
    void normalizeAndValidateMaintenance_throwsWhenDateIsInFuture() {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setTitle("Cambio");
        record.setCategory("Reparación");
        record.setDate(LocalDate.now().plusDays(1));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.normalizeAndValidateMaintenance(record)
        );

        assertEquals("La fecha del mantenimiento no puede estar en el futuro", ex.getMessage());
    }

    @Test
    void normalizeAndValidateMaintenance_throwsWhenKilometersAreNegative() {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setTitle("Cambio");
        record.setCategory("Reparación");
        record.setDate(LocalDate.now());
        record.setKilometers(-1);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.normalizeAndValidateMaintenance(record)
        );

        assertEquals("Los kilómetros no pueden ser negativos", ex.getMessage());
    }

    @Test
    void normalizeAndValidateMaintenance_throwsWhenPriceIsNegative() {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setTitle("Cambio");
        record.setCategory("Reparación");
        record.setDate(LocalDate.now());
        record.setPrice(new BigDecimal("-1"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.normalizeAndValidateMaintenance(record)
        );

        assertEquals("El precio no puede ser negativo", ex.getMessage());
    }

    @Test
    void normalizeAndValidateMaintenance_throwsWhenDescriptionTooLong() {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setTitle("Cambio");
        record.setCategory("Reparación");
        record.setDate(LocalDate.now());
        record.setDescription("a".repeat(1001));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.normalizeAndValidateMaintenance(record)
        );

        assertEquals("La descripción no puede superar 1000 caracteres", ex.getMessage());
    }
}
