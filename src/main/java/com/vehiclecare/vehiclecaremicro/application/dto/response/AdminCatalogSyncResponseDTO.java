package com.vehiclecare.vehiclecaremicro.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminCatalogSyncResponseDTO {
    private boolean executed;
    private boolean enabled;
    private String message;
}
