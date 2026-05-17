package com.vehiclecare.vehiclecaremicro.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vehiclecare.vehiclecaremicro.application.service.MaintenanceAttachmentService;
import com.vehiclecare.vehiclecaremicro.application.service.ValidationService;
import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.model.User;
import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import com.vehiclecare.vehiclecaremicro.domain.port.out.MaintenanceRepositoryPort;
import com.vehiclecare.vehiclecaremicro.domain.port.out.UserRepositoryPort;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.AuthenticationFailedException;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.BusinessValidationException;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.ConflictException;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class UseCaseTests {

    @Nested
    class AuthenticateUserUseCaseImplTest {

        @Mock
        private UserRepositoryPort userRepositoryPort;
        @Mock
        private PasswordEncoder passwordEncoder;
        @InjectMocks
        private AuthenticateUserUseCaseImpl useCase;

        @Test
        void authenticate_returnsUserWhenCredentialsAreValid() {
            User user = new User();
            user.setEmail("test@mail.com");
            user.setPassword("hashed");
            when(userRepositoryPort.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);

            User result = useCase.authenticate(" TEST@MAIL.COM ", "secret");

            assertSame(user, result);
        }

        @Test
        void authenticate_throwsWhenUserNotFound() {
            when(userRepositoryPort.findByEmail("test@mail.com")).thenReturn(Optional.empty());

            assertThrows(AuthenticationFailedException.class, () -> useCase.authenticate("test@mail.com", "secret"));
        }

        @Test
        void authenticate_throwsWhenPasswordDoesNotMatch() {
            User user = new User();
            user.setPassword("hashed");
            when(userRepositoryPort.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("secret", "hashed")).thenReturn(false);

            assertThrows(AuthenticationFailedException.class, () -> useCase.authenticate("test@mail.com", "secret"));
        }

        @Test
        void authenticate_throwsWhenEmailIsNull() {
            when(userRepositoryPort.findByEmail(null)).thenReturn(Optional.empty());

            assertThrows(AuthenticationFailedException.class, () -> useCase.authenticate(null, "secret"));
        }
    }

    @Nested
    class CreateUserUseCaseImplTest {

        @Mock
        private UserRepositoryPort userRepositoryPort;
        @Mock
        private PasswordEncoder passwordEncoder;
        @Mock
        private ValidationService validationService;
        @InjectMocks
        private CreateUserUseCaseImpl useCase;

        @Test
        void createUser_savesNormalizedUser() {
            User user = new User();
            user.setName("Alvaro");
            user.setEmail("test@mail.com");
            user.setPassword("plain");
            when(userRepositoryPort.existsByEmail("test@mail.com")).thenReturn(false);
            when(userRepositoryPort.findById(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode("plain")).thenReturn("hashed");
            when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0, User.class));

            User result = useCase.createUser(user);

            assertNotNull(result.getId());
            assertEquals("hashed", result.getPassword());
            verify(validationService).normalizeAndValidateUser(user);
        }

        @Test
        void createUser_keepsProvidedId() {
            User user = new User();
            user.setId("fixed123");
            user.setEmail("test@mail.com");
            user.setPassword("plain");
            when(userRepositoryPort.existsByEmail("test@mail.com")).thenReturn(false);
            when(passwordEncoder.encode("plain")).thenReturn("hashed");
            when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0, User.class));

            User result = useCase.createUser(user);

            assertEquals("fixed123", result.getId());
        }

        @Test
        void createUser_throwsWhenEmailAlreadyExists() {
            User user = new User();
            user.setEmail("test@mail.com");
            when(userRepositoryPort.existsByEmail("test@mail.com")).thenReturn(true);

            assertThrows(ConflictException.class, () -> useCase.createUser(user));
        }

        @Test
        void createUser_generatesAnotherIdWhenCollisionExists() {
            User user = new User();
            user.setEmail("test@mail.com");
            user.setPassword("plain");
            when(userRepositoryPort.existsByEmail("test@mail.com")).thenReturn(false);
            when(userRepositoryPort.findById(anyString())).thenReturn(Optional.of(new User()), Optional.empty());
            when(passwordEncoder.encode("plain")).thenReturn("hashed");
            when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0, User.class));

            User result = useCase.createUser(user);

            assertNotNull(result.getId());
            assertEquals(8, result.getId().length());
        }

        @Test
        void createUser_generatesIdWhenProvidedIdIsBlank() {
            User user = new User();
            user.setId("   ");
            user.setEmail("test@mail.com");
            user.setPassword("plain");
            when(userRepositoryPort.existsByEmail("test@mail.com")).thenReturn(false);
            when(userRepositoryPort.findById(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode("plain")).thenReturn("hashed");
            when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0, User.class));

            User result = useCase.createUser(user);

            assertNotNull(result.getId());
            assertEquals(8, result.getId().length());
        }
    }

    @Nested
    class CreateVehicleUseCaseImplTest {

        @Mock
        private VehicleRepositoryPort vehicleRepositoryPort;
        @Mock
        private UserRepositoryPort userRepositoryPort;
        @Mock
        private ValidationService validationService;
        @InjectMocks
        private CreateVehicleUseCaseImpl useCase;

        @Test
        void createVehicle_savesVehicleForUser() {
            Vehicle vehicle = validVehicle();
            when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(new User()));
            when(vehicleRepositoryPort.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0, Vehicle.class));

            Vehicle result = useCase.createVehicle("user-1", vehicle);

            assertEquals("user-1", result.getUserId());
            assertNotNull(result.getId());
            verify(validationService).normalizeAndValidateVehicle(vehicle);
        }

        @Test
        void createVehicle_keepsProvidedId() {
            Vehicle vehicle = validVehicle();
            vehicle.setId("veh-1");
            when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(new User()));
            when(vehicleRepositoryPort.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0, Vehicle.class));

            Vehicle result = useCase.createVehicle("user-1", vehicle);

            assertEquals("veh-1", result.getId());
        }

        @Test
        void createVehicle_throwsWhenUserNotFound() {
            when(userRepositoryPort.findById("user-1")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> useCase.createVehicle("user-1", validVehicle()));
        }

        @Test
        void createVehicle_generatesIdWhenProvidedIdIsBlank() {
            Vehicle vehicle = validVehicle();
            vehicle.setId("   ");
            when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(new User()));
            when(vehicleRepositoryPort.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0, Vehicle.class));

            Vehicle result = useCase.createVehicle("user-1", vehicle);

            assertNotNull(result.getId());
            assertEquals(8, result.getId().length());
        }
    }

    @Nested
    class UpdateVehicleUseCaseImplTest {

        @Mock
        private VehicleRepositoryPort vehicleRepositoryPort;
        @Mock
        private ValidationService validationService;
        @InjectMocks
        private UpdateVehicleUseCaseImpl useCase;

        @Test
        void updateVehicle_updatesExistingVehicle() {
            Vehicle existing = validVehicle();
            existing.setId("veh-1");
            existing.setUserId("user-1");
            Vehicle update = validVehicle();
            update.setUserId("user-1");
            update.setCurrentKilometers(123);
            when(vehicleRepositoryPort.findByIdAndUserId("veh-1", "user-1")).thenReturn(Optional.of(existing));
            when(vehicleRepositoryPort.save(existing)).thenReturn(existing);

            Vehicle result = useCase.updateVehicle("veh-1", "user-1", update);

            assertEquals(123, result.getCurrentKilometers());
            verify(validationService).normalizeAndValidateVehicle(update);
        }

        @Test
        void updateVehicle_allowsNullRequestUserId() {
            Vehicle existing = validVehicle();
            existing.setId("veh-1");
            existing.setUserId("user-1");
            Vehicle update = validVehicle();
            update.setUserId(null);
            when(vehicleRepositoryPort.findByIdAndUserId("veh-1", "user-1")).thenReturn(Optional.of(existing));
            when(vehicleRepositoryPort.save(existing)).thenReturn(existing);

            Vehicle result = useCase.updateVehicle("veh-1", "user-1", update);

            assertEquals(existing, result);
        }

        @Test
        void updateVehicle_throwsWhenVehicleNotFound() {
            when(vehicleRepositoryPort.findByIdAndUserId("veh-1", "user-1")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> useCase.updateVehicle("veh-1", "user-1", validVehicle()));
        }

        @Test
        void updateVehicle_throwsWhenOwnershipMismatch() {
            Vehicle existing = validVehicle();
            existing.setUserId("user-1");
            Vehicle update = validVehicle();
            update.setUserId("user-2");
            when(vehicleRepositoryPort.findByIdAndUserId("veh-1", "user-1")).thenReturn(Optional.of(existing));

            assertThrows(BusinessValidationException.class, () -> useCase.updateVehicle("veh-1", "user-1", update));
            verify(vehicleRepositoryPort, never()).save(any());
        }
    }

    @Nested
    class DeleteVehicleUseCaseImplTest {

        @Mock
        private VehicleRepositoryPort vehicleRepositoryPort;
        @InjectMocks
        private DeleteVehicleUseCaseImpl useCase;

        @Test
        void deleteVehicle_deletesWhenFound() {
            when(vehicleRepositoryPort.findByIdAndUserId("veh-1", "user-1")).thenReturn(Optional.of(validVehicle()));

            boolean result = useCase.deleteVehicle("veh-1", "user-1");

            assertTrue(result);
            verify(vehicleRepositoryPort).deleteById("veh-1");
        }

        @Test
        void deleteVehicle_throwsWhenNotFound() {
            when(vehicleRepositoryPort.findByIdAndUserId("veh-1", "user-1")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> useCase.deleteVehicle("veh-1", "user-1"));
        }
    }

    @Nested
    class GetVehicleUseCaseImplTest {

        @Mock
        private VehicleRepositoryPort vehicleRepositoryPort;
        @InjectMocks
        private GetVehicleUseCaseImpl useCase;

        @Test
        void getVehicleById_returnsVehicle() {
            Vehicle vehicle = validVehicle();
            when(vehicleRepositoryPort.findByIdAndUserId("veh-1", "user-1")).thenReturn(Optional.of(vehicle));

            Optional<Vehicle> result = useCase.getVehicleById("veh-1", "user-1");

            assertEquals(vehicle, result.orElseThrow());
        }

        @Test
        void getVehicleById_throwsWhenNotFound() {
            when(vehicleRepositoryPort.findByIdAndUserId("veh-1", "user-1")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> useCase.getVehicleById("veh-1", "user-1"));
        }
    }

    @Nested
    class ListVehiclesUseCaseImplTest {

        @Mock
        private VehicleRepositoryPort vehicleRepositoryPort;
        @InjectMocks
        private ListVehiclesUseCaseImpl useCase;

        @Test
        void listByUserId_returnsRepositoryResult() {
            List<Vehicle> vehicles = List.of(validVehicle());
            when(vehicleRepositoryPort.findByUserId("user-1")).thenReturn(vehicles);

            List<Vehicle> result = useCase.listByUserId("user-1");

            assertSame(vehicles, result);
        }
    }

    @Nested
    class AddMaintenanceRecordUseCaseImplTest {

        @Mock
        private MaintenanceRepositoryPort maintenanceRepositoryPort;
        @Mock
        private VehicleRepositoryPort vehicleRepositoryPort;
        @Mock
        private ValidationService validationService;
        @InjectMocks
        private AddMaintenanceRecordUseCaseImpl useCase;

        @Test
        void addMaintenanceRecord_savesRecord() {
            MaintenanceRecord record = validRecord();
            when(vehicleRepositoryPort.findByIdAndUserId("veh-1", "user-1")).thenReturn(Optional.of(validVehicle()));
            when(maintenanceRepositoryPort.save(any(MaintenanceRecord.class))).thenAnswer(invocation -> invocation.getArgument(0, MaintenanceRecord.class));

            MaintenanceRecord result = useCase.addMaintenanceRecord("veh-1", "user-1", record);

            assertEquals("veh-1", result.getVehicleId());
            assertNotNull(result.getId());
            verify(validationService).normalizeAndValidateMaintenance(record);
        }

        @Test
        void addMaintenanceRecord_keepsProvidedId() {
            MaintenanceRecord record = validRecord();
            record.setId("rec-1");
            when(vehicleRepositoryPort.findByIdAndUserId("veh-1", "user-1")).thenReturn(Optional.of(validVehicle()));
            when(maintenanceRepositoryPort.save(any(MaintenanceRecord.class))).thenAnswer(invocation -> invocation.getArgument(0, MaintenanceRecord.class));

            MaintenanceRecord result = useCase.addMaintenanceRecord("veh-1", "user-1", record);

            assertEquals("rec-1", result.getId());
        }

        @Test
        void addMaintenanceRecord_throwsWhenVehicleNotFound() {
            when(vehicleRepositoryPort.findByIdAndUserId("veh-1", "user-1")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> useCase.addMaintenanceRecord("veh-1", "user-1", validRecord()));
        }

        @Test
        void addMaintenanceRecord_generatesIdWhenProvidedIdIsBlank() {
            MaintenanceRecord record = validRecord();
            record.setId("   ");
            when(vehicleRepositoryPort.findByIdAndUserId("veh-1", "user-1")).thenReturn(Optional.of(validVehicle()));
            when(maintenanceRepositoryPort.save(any(MaintenanceRecord.class))).thenAnswer(invocation -> invocation.getArgument(0, MaintenanceRecord.class));

            MaintenanceRecord result = useCase.addMaintenanceRecord("veh-1", "user-1", record);

            assertNotNull(result.getId());
            assertEquals(8, result.getId().length());
        }
    }

    @Nested
    class UpdateMaintenanceRecordUseCaseImplTest {

        @Mock
        private MaintenanceRepositoryPort maintenanceRepositoryPort;
        @Mock
        private ValidationService validationService;
        @InjectMocks
        private UpdateMaintenanceRecordUseCaseImpl useCase;

        @Test
        void update_updatesExistingRecord() {
            MaintenanceRecord existing = validRecord();
            existing.setId("rec-1");
            existing.setVehicleId("veh-1");
            MaintenanceRecord update = validRecord();
            update.setVehicleId("veh-1");
            update.setKilometers(10);
            when(maintenanceRepositoryPort.findByIdAndUserId("rec-1", "user-1")).thenReturn(Optional.of(existing));
            when(maintenanceRepositoryPort.save(existing)).thenReturn(existing);

            MaintenanceRecord result = useCase.update("rec-1", "user-1", update);

            assertEquals(10, result.getKilometers());
            verify(validationService).normalizeAndValidateMaintenance(update);
        }

        @Test
        void update_allowsNullVehicleIdInRequest() {
            MaintenanceRecord existing = validRecord();
            existing.setId("rec-1");
            existing.setVehicleId("veh-1");
            MaintenanceRecord update = validRecord();
            update.setVehicleId(null);
            when(maintenanceRepositoryPort.findByIdAndUserId("rec-1", "user-1")).thenReturn(Optional.of(existing));
            when(maintenanceRepositoryPort.save(existing)).thenReturn(existing);

            MaintenanceRecord result = useCase.update("rec-1", "user-1", update);

            assertEquals(existing, result);
        }

        @Test
        void update_throwsWhenRecordNotFound() {
            when(maintenanceRepositoryPort.findByIdAndUserId("rec-1", "user-1")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> useCase.update("rec-1", "user-1", validRecord()));
        }

        @Test
        void update_throwsWhenVehicleMismatch() {
            MaintenanceRecord existing = validRecord();
            existing.setVehicleId("veh-1");
            MaintenanceRecord update = validRecord();
            update.setVehicleId("veh-2");
            when(maintenanceRepositoryPort.findByIdAndUserId("rec-1", "user-1")).thenReturn(Optional.of(existing));

            assertThrows(BusinessValidationException.class, () -> useCase.update("rec-1", "user-1", update));
        }
    }

    @Nested
    class DeleteMaintenanceRecordUseCaseImplTest {

        @Mock
        private MaintenanceRepositoryPort maintenanceRepositoryPort;
        @Mock
        private MaintenanceAttachmentService maintenanceAttachmentService;
        @InjectMocks
        private DeleteMaintenanceRecordUseCaseImpl useCase;

        @Test
        void delete_deletesRecordAndAttachments() {
            when(maintenanceRepositoryPort.findByIdAndUserId("rec-1", "user-1")).thenReturn(Optional.of(validRecord()));

            boolean result = useCase.delete("rec-1", "user-1");

            assertTrue(result);
            verify(maintenanceAttachmentService).deleteAllFromRecord("rec-1");
            verify(maintenanceRepositoryPort).deleteById("rec-1");
        }

        @Test
        void delete_throwsWhenRecordNotFound() {
            when(maintenanceRepositoryPort.findByIdAndUserId("rec-1", "user-1")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> useCase.delete("rec-1", "user-1"));
        }
    }

    @Nested
    class GetMaintenanceRecordUseCaseImplTest {

        @Mock
        private MaintenanceRepositoryPort maintenanceRepositoryPort;
        @InjectMocks
        private GetMaintenanceRecordUseCaseImpl useCase;

        @Test
        void getById_returnsRecord() {
            MaintenanceRecord record = validRecord();
            when(maintenanceRepositoryPort.findByIdAndUserId("rec-1", "user-1")).thenReturn(Optional.of(record));

            Optional<MaintenanceRecord> result = useCase.getById("rec-1", "user-1");

            assertSame(record, result.orElseThrow());
        }

        @Test
        void getById_throwsWhenNotFound() {
            when(maintenanceRepositoryPort.findByIdAndUserId("rec-1", "user-1")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> useCase.getById("rec-1", "user-1"));
        }
    }

    @Nested
    class ListMaintenanceRecordsUseCaseImplTest {

        @Mock
        private MaintenanceRepositoryPort maintenanceRepositoryPort;
        @Mock
        private VehicleRepositoryPort vehicleRepositoryPort;
        @InjectMocks
        private ListMaintenanceRecordsUseCaseImpl useCase;

        @Test
        void listByVehicleId_returnsRecordsWhenVehicleBelongsToUser() {
            List<MaintenanceRecord> records = List.of(validRecord());
            when(vehicleRepositoryPort.findByIdAndUserId("veh-1", "user-1")).thenReturn(Optional.of(validVehicle()));
            when(maintenanceRepositoryPort.findByVehicleIdAndUserId("veh-1", "user-1")).thenReturn(records);

            List<MaintenanceRecord> result = useCase.listByVehicleId("veh-1", "user-1");

            assertSame(records, result);
        }

        @Test
        void listByVehicleId_throwsWhenVehicleNotFound() {
            when(vehicleRepositoryPort.findByIdAndUserId("veh-1", "user-1")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> useCase.listByVehicleId("veh-1", "user-1"));
        }
    }

    private Vehicle validVehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("BMW");
        vehicle.setModel("320i");
        vehicle.setYear(2020);
        vehicle.setLicensePlate("1234BCD");
        vehicle.setVin("WBA12345678901234");
        vehicle.setFuelType("Gasolina");
        vehicle.setCurrentKilometers(1000);
        return vehicle;
    }

    private MaintenanceRecord validRecord() {
        MaintenanceRecord record = new MaintenanceRecord();
        record.setTitle("Cambio aceite");
        record.setCategory("Reparación");
        record.setDate(LocalDate.now());
        record.setKilometers(1000);
        record.setPrice(BigDecimal.ONE);
        record.setDescription("ok");
        return record;
    }
}
