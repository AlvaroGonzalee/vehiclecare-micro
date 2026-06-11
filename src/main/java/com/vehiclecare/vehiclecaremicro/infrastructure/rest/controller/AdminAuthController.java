package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.dto.request.AuthLoginRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.AdminAuthResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.AdminAuthenticationService;
import com.vehiclecare.vehiclecaremicro.application.service.JwtService;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.AdminEntity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
@Slf4j
public class AdminAuthController {

    private final AdminAuthenticationService adminAuthenticationService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AdminAuthResponseDTO> login(@Valid @RequestBody AuthLoginRequestDTO requestDTO) {
        log.info("Admin login request received for email={}", requestDTO.getEmail());
        AdminEntity admin = adminAuthenticationService.authenticate(requestDTO.getEmail(), requestDTO.getPassword());
        return ResponseEntity.ok(
                new AdminAuthResponseDTO(
                        admin.getId(),
                        admin.getEmail(),
                        jwtService.generateAdminToken(admin.getId(), admin.getEmail())
                )
        );
    }
}
