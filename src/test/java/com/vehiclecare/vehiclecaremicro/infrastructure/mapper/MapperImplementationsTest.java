package com.vehiclecare.vehiclecaremicro.infrastructure.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.vehiclecare.vehiclecaremicro.application.dto.request.AttachmentRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.request.MaintenanceRecordCreateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.request.MaintenanceRecordUpdateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.request.VehicleCreateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.request.VehicleUpdateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.AttachmentResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.BrandResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.MaintenanceRecordResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.ModelResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.UserResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.VehicleResponseDTO;
import com.vehiclecare.vehiclecaremicro.domain.model.Attachment;
import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.domain.model.User;
import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.AttachmentEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.BrandEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.MaintenanceRecordEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.ModelEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.UserEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.VehicleEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MapperImplementationsTest {

    private UserReferenceMapperImpl userReferenceMapper;
    private VehicleReferenceMapperImpl vehicleReferenceMapper;
    private MaintenanceRecordReferenceMapperImpl maintenanceRecordReferenceMapper;
    private UserMapperImpl userMapper;
    private BrandMapperImpl brandMapper;
    private ModelMapperImpl modelMapper;
    private VehicleMapperImpl vehicleMapper;
    private AttachmentMapperImpl attachmentMapper;
    private MaintenanceRecordMapperImpl maintenanceRecordMapper;

    @BeforeEach
    void setUp() {
        userReferenceMapper = new UserReferenceMapperImpl();
        vehicleReferenceMapper = new VehicleReferenceMapperImpl();
        maintenanceRecordReferenceMapper = new MaintenanceRecordReferenceMapperImpl();
        userMapper = new UserMapperImpl();
        brandMapper = new BrandMapperImpl();
        modelMapper = new ModelMapperImpl();
        vehicleMapper = new VehicleMapperImpl();
        attachmentMapper = new AttachmentMapperImpl();
        maintenanceRecordMapper = new MaintenanceRecordMapperImpl();
        ReflectionTestUtils.setField(vehicleMapper, "userReferenceMapper", userReferenceMapper);
        ReflectionTestUtils.setField(attachmentMapper, "maintenanceRecordReferenceMapper", maintenanceRecordReferenceMapper);
        ReflectionTestUtils.setField(maintenanceRecordMapper, "attachmentMapper", attachmentMapper);
        ReflectionTestUtils.setField(maintenanceRecordMapper, "vehicleReferenceMapper", vehicleReferenceMapper);
    }

    @Test
    void referenceMappers_returnNullOrEntityReference() {
        assertNull(userReferenceMapper.toReference(null));
        assertEquals("u1", userReferenceMapper.toReference("u1").getId());
        assertNull(vehicleReferenceMapper.toReference(null));
        assertEquals("v1", vehicleReferenceMapper.toReference("v1").getId());
        assertNull(maintenanceRecordReferenceMapper.toReference(null));
        assertEquals("m1", maintenanceRecordReferenceMapper.toReference("m1").getId());
    }

    @Test
    void userMapper_mapsAllDirectionsAndNulls() {
        LocalDateTime now = LocalDateTime.now();
        UserEntity entity = UserEntity.builder()
                .id("u1")
                .name("Alvaro")
                .email("a@a.com")
                .password("secret")
                .profileImageUrl("profiles/a.jpg")
                .createdAt(now)
                .updatedAt(now)
                .build();
        User domain = userMapper.toDomain(entity);
        assertEquals("u1", domain.getId());
        assertEquals("secret", domain.getPassword());

        UserEntity mappedEntity = userMapper.toEntity(domain);
        assertEquals("Alvaro", mappedEntity.getName());

        UserResponseDTO response = userMapper.toResponse(domain);
        assertEquals("a@a.com", response.getEmail());

        assertNull(userMapper.toDomain((UserEntity) null));
        assertNull(userMapper.toEntity((User) null));
        assertNull(userMapper.toResponse(null));
    }

    @Test
    void brandAndModelMappers_mapAndHandleNestedNulls() {
        BrandEntity brand = BrandEntity.builder().id("b1").name("BMW").logoUrl("logo").active(true).build();
        BrandResponseDTO brandResponse = brandMapper.toResponse(brand);
        assertEquals("BMW", brandResponse.getName());
        assertNull(brandMapper.toResponse(null));

        ModelEntity model = ModelEntity.builder().id("m1").name("320i").brand(brand).active(true).build();
        ModelResponseDTO modelResponse = modelMapper.toResponse(model);
        assertEquals("b1", modelResponse.getBrandId());

        ModelEntity modelWithoutBrand = ModelEntity.builder().id("m2").name("330i").brand(null).build();
        assertNull(modelMapper.toResponse(modelWithoutBrand).getBrandId());
        assertNull(modelMapper.toResponse(null));
    }

    @Test
    void vehicleMapper_mapsEntityDomainDtosAndNullPaths() {
        UserEntity userEntity = UserEntity.builder().id("u1").build();
        VehicleEntity entity = VehicleEntity.builder()
                .id("v1")
                .user(userEntity)
                .brand("BMW")
                .model("320i")
                .vehicleYear(2020)
                .licensePlate("1234 BCD")
                .vin("VIN12345678901234")
                .currentKilometers(1000)
                .fuelType("Gasolina")
                .imageUrl("vehicles/v1.jpg")
                .build();

        Vehicle domain = vehicleMapper.toDomain(entity);
        assertEquals("u1", domain.getUserId());
        assertEquals(2020, domain.getYear());

        VehicleEntity mappedEntity = vehicleMapper.toEntity(domain);
        assertEquals("u1", mappedEntity.getUser().getId());
        assertEquals(2020, mappedEntity.getVehicleYear());

        VehicleCreateRequestDTO createDto = new VehicleCreateRequestDTO("u1", "BMW", "320i", 2020, "1234BCD", "VIN12345678901234", 1000, "Gasolina");
        VehicleUpdateRequestDTO updateDto = new VehicleUpdateRequestDTO("BMW", "330i", 2021, "1234BCD", null, 2000, "Diésel");
        assertEquals("u1", vehicleMapper.toDomain(createDto).getUserId());
        assertEquals("330i", vehicleMapper.toDomain(updateDto).getModel());

        VehicleResponseDTO response = vehicleMapper.toResponse(domain);
        assertEquals("vehicles/v1.jpg", response.getImageUrl());

        VehicleEntity entityWithoutUser = VehicleEntity.builder().id("v2").user(null).vehicleYear(2020).build();
        assertNull(vehicleMapper.toDomain(entityWithoutUser).getUserId());
        VehicleEntity entityWithUserWithoutId = VehicleEntity.builder().id("v3").user(UserEntity.builder().build()).vehicleYear(2020).build();
        assertNull(vehicleMapper.toDomain(entityWithUserWithoutId).getUserId());

        assertNull(vehicleMapper.toDomain((VehicleEntity) null));
        assertNull(vehicleMapper.toEntity((Vehicle) null));
        assertNull(vehicleMapper.toDomain((VehicleCreateRequestDTO) null));
        assertNull(vehicleMapper.toDomain((VehicleUpdateRequestDTO) null));
        assertNull(vehicleMapper.toResponse(null));
    }

    @Test
    void attachmentMapper_mapsEntityDomainDtosAndNullPaths() {
        MaintenanceRecordEntity record = MaintenanceRecordEntity.builder().id("r1").build();
        AttachmentEntity entity = AttachmentEntity.builder()
                .id("a1")
                .maintenanceRecord(record)
                .fileName("doc.pdf")
                .fileType("application/pdf")
                .filePath("records/r1/doc.pdf")
                .build();

        Attachment domain = attachmentMapper.toDomain(entity);
        assertEquals("r1", domain.getMaintenanceRecordId());

        AttachmentEntity mappedEntity = attachmentMapper.toEntity(domain);
        assertEquals("r1", mappedEntity.getMaintenanceRecord().getId());

        AttachmentRequestDTO request = new AttachmentRequestDTO("a1", "doc.pdf", "application/pdf", "records/r1/doc.pdf");
        Attachment fromRequest = attachmentMapper.toDomain(request);
        assertEquals("doc.pdf", fromRequest.getFileName());

        AttachmentResponseDTO response = attachmentMapper.toResponse(domain);
        assertEquals("records/r1/doc.pdf", response.getFilePath());

        AttachmentEntity entityWithoutRecord = AttachmentEntity.builder().id("a2").maintenanceRecord(null).build();
        assertNull(attachmentMapper.toDomain(entityWithoutRecord).getMaintenanceRecordId());
        AttachmentEntity entityWithRecordWithoutId = AttachmentEntity.builder().id("a3").maintenanceRecord(MaintenanceRecordEntity.builder().build()).build();
        assertNull(attachmentMapper.toDomain(entityWithRecordWithoutId).getMaintenanceRecordId());

        assertNull(attachmentMapper.toDomain((AttachmentEntity) null));
        assertNull(attachmentMapper.toEntity((Attachment) null));
        assertNull(attachmentMapper.toDomain((AttachmentRequestDTO) null));
        assertNull(attachmentMapper.toResponse(null));
    }

    @Test
    void maintenanceRecordMapper_mapsEntityDomainDtosAndNullPaths() {
        VehicleEntity vehicleEntity = VehicleEntity.builder().id("v1").build();
        Attachment attachment = new Attachment("a1", "r1", "doc.pdf", "application/pdf", "records/r1/doc.pdf");
        AttachmentEntity attachmentEntity = AttachmentEntity.builder()
                .id("a1")
                .maintenanceRecord(MaintenanceRecordEntity.builder().id("r1").build())
                .fileName("doc.pdf")
                .fileType("application/pdf")
                .filePath("records/r1/doc.pdf")
                .build();
        MaintenanceRecordEntity entity = MaintenanceRecordEntity.builder()
                .id("r1")
                .vehicle(vehicleEntity)
                .title("Cambio")
                .maintenanceDate(LocalDate.now())
                .category("Reparación")
                .kilometers(1000)
                .price(BigDecimal.ONE)
                .description("Desc")
                .attachments(List.of(attachmentEntity))
                .build();

        MaintenanceRecord domain = maintenanceRecordMapper.toDomain(entity);
        assertEquals("v1", domain.getVehicleId());
        assertNotNull(domain.getAttachments());

        MaintenanceRecordEntity mappedEntity = maintenanceRecordMapper.toEntity(domain);
        assertEquals("v1", mappedEntity.getVehicle().getId());
        assertEquals("a1", mappedEntity.getAttachments().get(0).getId());

        AttachmentRequestDTO attachmentRequest = new AttachmentRequestDTO("a1", "doc.pdf", "application/pdf", "records/r1/doc.pdf");
        MaintenanceRecordCreateRequestDTO createDto = new MaintenanceRecordCreateRequestDTO(
                "Cambio", LocalDate.now(), "Reparación", 1000, BigDecimal.ONE, "Desc", List.of(attachmentRequest)
        );
        MaintenanceRecordUpdateRequestDTO updateDto = new MaintenanceRecordUpdateRequestDTO(
                "Cambio", LocalDate.now(), "Mejora", 1001, BigDecimal.TEN, "Desc2", List.of(attachmentRequest)
        );
        assertEquals("Reparación", maintenanceRecordMapper.toDomain(createDto).getCategory());
        assertEquals("Mejora", maintenanceRecordMapper.toDomain(updateDto).getCategory());

        MaintenanceRecord recordWithoutVehicle = new MaintenanceRecord();
        recordWithoutVehicle.setId("r1");
        recordWithoutVehicle.setVehicleId("v1");
        recordWithoutVehicle.setTitle("Cambio");
        recordWithoutVehicle.setDate(LocalDate.now());
        recordWithoutVehicle.setCategory("Reparación");
        recordWithoutVehicle.setAttachments(List.of(attachment));
        MaintenanceRecordResponseDTO response = maintenanceRecordMapper.toResponse(recordWithoutVehicle);
        assertEquals("a1", response.getAttachments().get(0).getId());

        MaintenanceRecordEntity entityWithoutVehicle = MaintenanceRecordEntity.builder().id("r2").vehicle(null).build();
        assertNull(maintenanceRecordMapper.toDomain(entityWithoutVehicle).getVehicleId());
        MaintenanceRecordEntity entityWithVehicleWithoutId = MaintenanceRecordEntity.builder()
                .id("r3")
                .vehicle(VehicleEntity.builder().build())
                .build();
        assertNull(maintenanceRecordMapper.toDomain(entityWithVehicleWithoutId).getVehicleId());

        assertNull(maintenanceRecordMapper.toDomain((MaintenanceRecordEntity) null));
        assertNull(maintenanceRecordMapper.toEntity((MaintenanceRecord) null));
        assertNull(maintenanceRecordMapper.toDomain((MaintenanceRecordCreateRequestDTO) null));
        assertNull(maintenanceRecordMapper.toDomain((MaintenanceRecordUpdateRequestDTO) null));
        assertNull(maintenanceRecordMapper.toResponse(null));
    }
}
