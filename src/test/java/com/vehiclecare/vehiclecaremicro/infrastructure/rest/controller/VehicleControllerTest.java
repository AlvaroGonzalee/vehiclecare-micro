package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vehiclecare.vehiclecaremicro.application.dto.request.VehicleCreateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.request.VehicleUpdateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.FileUploadResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.VehicleResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.MinioStorageService;
import com.vehiclecare.vehiclecaremicro.application.service.PublicFileUrlService;
import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import com.vehiclecare.vehiclecaremicro.domain.port.in.CreateVehicleUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.DeleteVehicleUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.GetVehicleUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.ListVehiclesUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.UpdateVehicleUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.VehicleRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.VehicleMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.OwnershipAccessException;
import com.vehiclecare.vehiclecaremicro.infrastructure.security.AuthenticatedUser;
import com.vehiclecare.vehiclecaremicro.infrastructure.security.AuthenticationContext;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    @Mock private CreateVehicleUseCase createVehicleUseCase;
    @Mock private GetVehicleUseCase getVehicleUseCase;
    @Mock private ListVehiclesUseCase listVehiclesUseCase;
    @Mock private UpdateVehicleUseCase updateVehicleUseCase;
    @Mock private DeleteVehicleUseCase deleteVehicleUseCase;
    @Mock private VehicleRepositoryPort vehicleRepositoryPort;
    @Mock private MinioStorageService minioStorageService;
    @Mock private PublicFileUrlService publicFileUrlService;
    @Mock private VehicleMapper vehicleMapper;
    @Mock private AuthenticationContext authenticationContext;

    private VehicleController controller;

    @BeforeEach
    void setUp() {
        controller = new VehicleController(
                createVehicleUseCase, getVehicleUseCase, listVehiclesUseCase, updateVehicleUseCase,
                deleteVehicleUseCase, vehicleRepositoryPort, minioStorageService, publicFileUrlService,
                vehicleMapper, authenticationContext
        );
    }

    @Test
    void listVehicles_returnsOwnedVehicles() {
        MockHttpServletRequest request = request("user-1");
        Vehicle vehicle = vehicle();
        VehicleResponseDTO dto = vehicleDto();
        when(listVehiclesUseCase.listByUserId("user-1")).thenReturn(List.of(vehicle));
        when(vehicleMapper.toResponse(vehicle)).thenReturn(dto);
        when(publicFileUrlService.buildObjectUrl(request, "vehicles/1.jpg")).thenReturn("http://public");

        var response = controller.listVehicles("user-1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void listVehicles_throwsWhenQueryUserDiffers() {
        MockHttpServletRequest request = request("user-1");

        assertThrows(OwnershipAccessException.class, () -> controller.listVehicles("user-2", request));
    }

    @Test
    void listVehicles_allowsMissingQueryUserId() {
        MockHttpServletRequest request = request("user-1");
        Vehicle vehicle = vehicle();
        VehicleResponseDTO dto = vehicleDto();
        when(listVehiclesUseCase.listByUserId("user-1")).thenReturn(List.of(vehicle));
        when(vehicleMapper.toResponse(vehicle)).thenReturn(dto);
        when(publicFileUrlService.buildObjectUrl(request, "vehicles/1.jpg")).thenReturn("http://public");

        var response = controller.listVehicles(null, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getVehicle_returnsVehicleWhenFound() {
        MockHttpServletRequest request = request("user-1");
        Vehicle vehicle = vehicle();
        VehicleResponseDTO dto = vehicleDto();
        when(getVehicleUseCase.getVehicleById("veh-1", "user-1")).thenReturn(Optional.of(vehicle));
        when(vehicleMapper.toResponse(vehicle)).thenReturn(dto);
        when(publicFileUrlService.buildObjectUrl(request, "vehicles/1.jpg")).thenReturn("http://public");

        var response = controller.getVehicle("veh-1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("http://public", response.getBody().getImageUrl());
    }

    @Test
    void getVehicle_returnsNotFoundWhenMissing() {
        MockHttpServletRequest request = request("user-1");
        when(getVehicleUseCase.getVehicleById("veh-1", "user-1")).thenReturn(Optional.empty());

        var response = controller.getVehicle("veh-1", request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createVehicle_returnsCreated() {
        MockHttpServletRequest request = request("user-1");
        VehicleCreateRequestDTO dto = new VehicleCreateRequestDTO("user-1", "BMW", "320i", 2020, "1234BCD", null, null, "Gasolina");
        Vehicle vehicle = vehicle();
        VehicleResponseDTO responseDto = vehicleDto();
        when(vehicleMapper.toDomain(dto)).thenReturn(vehicle);
        when(createVehicleUseCase.createVehicle("user-1", vehicle)).thenReturn(vehicle);
        when(vehicleMapper.toResponse(vehicle)).thenReturn(responseDto);
        when(publicFileUrlService.buildObjectUrl(request, "vehicles/1.jpg")).thenReturn("http://public");

        var response = controller.createVehicle(dto, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void createVehicle_throwsWhenCreatingForAnotherUser() {
        MockHttpServletRequest request = request("user-1");
        VehicleCreateRequestDTO dto = new VehicleCreateRequestDTO("user-2", "BMW", "320i", 2020, "1234BCD", null, null, "Gasolina");

        assertThrows(OwnershipAccessException.class, () -> controller.createVehicle(dto, request));
    }

    @Test
    void updateVehicle_returnsUpdated() {
        MockHttpServletRequest request = request("user-1");
        VehicleUpdateRequestDTO dto = new VehicleUpdateRequestDTO("BMW", "320i", 2020, "1234BCD", null, 1000, "Gasolina");
        Vehicle vehicle = vehicle();
        VehicleResponseDTO responseDto = vehicleDto();
        when(vehicleMapper.toDomain(dto)).thenReturn(vehicle);
        when(updateVehicleUseCase.updateVehicle("veh-1", "user-1", vehicle)).thenReturn(vehicle);
        when(vehicleMapper.toResponse(vehicle)).thenReturn(responseDto);
        when(publicFileUrlService.buildObjectUrl(request, "vehicles/1.jpg")).thenReturn("http://public");

        var response = controller.updateVehicle("veh-1", dto, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteVehicle_returnsNoContentWhenDeleted() {
        MockHttpServletRequest request = request("user-1");
        when(deleteVehicleUseCase.deleteVehicle("veh-1", "user-1")).thenReturn(true);

        var response = controller.deleteVehicle("veh-1", request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void deleteVehicle_returnsNotFoundWhenNotDeleted() {
        MockHttpServletRequest request = request("user-1");
        when(deleteVehicleUseCase.deleteVehicle("veh-1", "user-1")).thenReturn(false);

        var response = controller.deleteVehicle("veh-1", request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void uploadVehicleImage_returnsUpdatedVehicle() {
        MockHttpServletRequest request = request("user-1");
        MockMultipartFile file = new MockMultipartFile("file", "vehicle.jpg", "image/jpeg", new byte[] {1});
        Vehicle vehicle = vehicle();
        VehicleResponseDTO responseDto = vehicleDto();
        when(vehicleRepositoryPort.findByIdAndUserId("veh-1", "user-1")).thenReturn(Optional.of(vehicle));
        when(minioStorageService.uploadImage(file, "vehicles/veh-1"))
                .thenReturn(new FileUploadResponseDTO("vehicle.jpg", "image/jpeg", 1L, "vehicles/veh-1/a.jpg", "internal"));
        when(vehicleRepositoryPort.save(any(Vehicle.class))).thenReturn(vehicle);
        when(vehicleMapper.toResponse(vehicle)).thenReturn(responseDto);
        when(publicFileUrlService.buildObjectUrl(request, "vehicles/1.jpg")).thenReturn("http://public");

        var response = controller.uploadVehicleImage("veh-1", file, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(minioStorageService).uploadImage(file, "vehicles/veh-1");
    }

    @Test
    void uploadVehicleImage_throwsWhenVehicleMissing() {
        MockHttpServletRequest request = request("user-1");
        MockMultipartFile file = new MockMultipartFile("file", "vehicle.jpg", "image/jpeg", new byte[] {1});
        when(vehicleRepositoryPort.findByIdAndUserId("veh-1", "user-1")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.uploadVehicleImage("veh-1", file, request)
        );

        assertEquals("Vehículo no encontrado", ex.getMessage());
    }

    private MockHttpServletRequest request(String userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(authenticationContext.requireCurrentUser(request)).thenReturn(new AuthenticatedUser(userId, "a@a.com"));
        return request;
    }

    private Vehicle vehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setId("veh-1");
        vehicle.setUserId("user-1");
        vehicle.setBrand("BMW");
        vehicle.setModel("320i");
        vehicle.setYear(2020);
        vehicle.setLicensePlate("1234 BCD");
        vehicle.setImageUrl("vehicles/1.jpg");
        return vehicle;
    }

    private VehicleResponseDTO vehicleDto() {
        return new VehicleResponseDTO("veh-1", "user-1", "BMW", "320i", 2020, "1234 BCD", null, null, "Gasolina", "vehicles/1.jpg");
    }
}
