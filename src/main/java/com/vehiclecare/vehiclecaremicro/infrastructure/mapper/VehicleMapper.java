package com.vehiclecare.vehiclecaremicro.infrastructure.mapper;

import com.vehiclecare.vehiclecaremicro.application.dto.request.VehicleRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.VehicleResponseDTO;
import com.vehiclecare.vehiclecaremicro.domain.model.Vehicle;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.VehicleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {UserReferenceMapper.class})
public interface VehicleMapper {

    @Mapping(target = "userId", source = "user.id")
    Vehicle toDomain(VehicleEntity entity);

    @Mapping(target = "user", source = "userId", qualifiedByName = "userRef")
    VehicleEntity toEntity(Vehicle vehicle);

    Vehicle toDomain(VehicleRequestDTO requestDTO);

    VehicleResponseDTO toResponse(Vehicle vehicle);
}
