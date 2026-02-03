package com.vehiclecare.vehiclecaremicro.infrastructure.mapper;

import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.MaintenanceRecordEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface MaintenanceRecordReferenceMapper {

    @Named("maintenanceRef")
    default MaintenanceRecordEntity toReference(String id) {
        if (id == null) {
            return null;
        }
        MaintenanceRecordEntity entity = new MaintenanceRecordEntity();
        entity.setId(id);
        return entity;
    }
}
