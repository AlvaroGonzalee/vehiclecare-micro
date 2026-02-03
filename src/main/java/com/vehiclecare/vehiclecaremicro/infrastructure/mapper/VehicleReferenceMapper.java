package com.vehiclecare.vehiclecaremicro.infrastructure.mapper;

import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.VehicleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface VehicleReferenceMapper {

    @Named("vehicleRef")
    default VehicleEntity toReference(String id) {
        if (id == null) {
            return null;
        }
        VehicleEntity entity = new VehicleEntity();
        entity.setId(id);
        return entity;
    }
}
