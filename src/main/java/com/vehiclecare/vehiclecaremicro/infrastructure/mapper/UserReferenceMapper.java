package com.vehiclecare.vehiclecaremicro.infrastructure.mapper;

import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserReferenceMapper {

    @Named("userRef")
    default UserEntity toReference(String id) {
        if (id == null) {
            return null;
        }
        UserEntity entity = new UserEntity();
        entity.setId(id);
        return entity;
    }
}
