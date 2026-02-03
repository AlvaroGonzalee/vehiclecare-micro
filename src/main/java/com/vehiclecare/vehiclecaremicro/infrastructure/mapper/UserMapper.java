package com.vehiclecare.vehiclecaremicro.infrastructure.mapper;

import com.vehiclecare.vehiclecaremicro.application.dto.request.UserRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.UserResponseDTO;
import com.vehiclecare.vehiclecaremicro.domain.model.User;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserMapper {

    User toDomain(UserEntity entity);

    UserEntity toEntity(User user);

    User toDomain(UserRequestDTO requestDTO);

    UserResponseDTO toResponse(User user);
}
