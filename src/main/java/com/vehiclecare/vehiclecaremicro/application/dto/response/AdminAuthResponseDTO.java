package com.vehiclecare.vehiclecaremicro.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminAuthResponseDTO {
    private String id;
    private String email;
    private String token;
}
