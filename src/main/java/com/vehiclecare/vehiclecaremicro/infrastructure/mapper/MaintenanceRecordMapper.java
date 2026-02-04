package com.vehiclecare.vehiclecaremicro.infrastructure.mapper;

import com.vehiclecare.vehiclecaremicro.application.dto.request.MaintenanceRecordRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.MaintenanceRecordResponseDTO;
import com.vehiclecare.vehiclecaremicro.domain.model.MaintenanceRecord;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.MaintenanceRecordEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AttachmentMapper.class, VehicleReferenceMapper.class})
public interface MaintenanceRecordMapper {

    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "date", source = "maintenanceDate")
    MaintenanceRecord toDomain(MaintenanceRecordEntity entity);

    @Mapping(target = "vehicle", source = "vehicleId", qualifiedByName = "vehicleRef")
    @Mapping(target = "maintenanceDate", source = "date")
    MaintenanceRecordEntity toEntity(MaintenanceRecord maintenanceRecord);

    MaintenanceRecord toDomain(MaintenanceRecordRequestDTO requestDTO);

    MaintenanceRecordResponseDTO toResponse(MaintenanceRecord maintenanceRecord);
}
