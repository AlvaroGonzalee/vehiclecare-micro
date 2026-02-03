package com.vehiclecare.vehiclecaremicro.domain.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Attachment {
    private String id;
    private String maintenanceRecordId;
    private String fileName;
    private String fileType;
    private String filePath;
}
