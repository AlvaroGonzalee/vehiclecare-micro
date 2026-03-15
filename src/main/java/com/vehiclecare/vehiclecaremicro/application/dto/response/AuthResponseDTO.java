package com.vehiclecare.vehiclecaremicro.application.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponseDTO {
    private String id;
    private String name;
    private String email;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private String token;
}
