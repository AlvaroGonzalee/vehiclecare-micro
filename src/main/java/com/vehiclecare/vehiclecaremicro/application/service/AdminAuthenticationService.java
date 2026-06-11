package com.vehiclecare.vehiclecaremicro.application.service;

import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.AdminEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.AdminJpaRepository;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.AuthenticationFailedException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthenticationService {

    private final AdminJpaRepository adminJpaRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminEntity authenticate(String email, String password) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        log.info("Authenticating admin email={}", normalizedEmail);
        AdminEntity admin = adminJpaRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new AuthenticationFailedException("Credenciales de administrador inválidas"));

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            log.warn("Admin authentication failed due to password mismatch email={}", normalizedEmail);
            throw new AuthenticationFailedException("Credenciales de administrador inválidas");
        }

        log.info("Admin authenticated successfully adminId={} email={}", admin.getId(), admin.getEmail());
        return admin;
    }
}
