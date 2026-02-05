package com.vehiclecare.vehiclecaremicro.infrastructure.mapper;

import com.vehiclecare.vehiclecaremicro.application.dto.request.VehicleCreateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.request.VehicleUpdateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.VehicleResponseDTO;
import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.VehicleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {UserReferenceMapper.class})
public interface VehicleMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "year", source = "vehicleYear")
    Vehicle toDomain(VehicleEntity entity);

    @Mapping(target = "user", source = "userId", qualifiedByName = "userRef")
    @Mapping(target = "vehicleYear", source = "year")
    VehicleEntity toEntity(Vehicle vehicle);

    Vehicle toDomain(VehicleCreateRequestDTO requestDTO);

    Vehicle toDomain(VehicleUpdateRequestDTO requestDTO);

    VehicleResponseDTO toResponse(Vehicle vehicle);
}
