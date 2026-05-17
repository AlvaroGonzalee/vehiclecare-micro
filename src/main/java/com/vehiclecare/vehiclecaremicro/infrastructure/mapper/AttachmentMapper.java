package com.vehiclecare.vehiclecaremicro.infrastructure.mapper;

import com.vehiclecare.vehiclecaremicro.application.dto.request.AttachmentRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.AttachmentResponseDTO;
import com.vehiclecare.vehiclecaremicro.domain.model.Attachment;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.AttachmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MaintenanceRecordReferenceMapper.class})
public interface AttachmentMapper {

    @Mapping(target = "maintenanceRecordId", source = "maintenanceRecord.id")
    Attachment toDomain(AttachmentEntity entity);

    @Mapping(target = "maintenanceRecord", source = "maintenanceRecordId", qualifiedByName = "maintenanceRef")
    AttachmentEntity toEntity(Attachment attachment);

    @Mapping(target = "maintenanceRecordId", ignore = true)
    Attachment toDomain(AttachmentRequestDTO requestDTO);

    AttachmentResponseDTO toResponse(Attachment attachment);
}
