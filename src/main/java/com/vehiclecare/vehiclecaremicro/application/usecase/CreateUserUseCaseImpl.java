package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.domain.model.User;
import com.vehiclecare.vehiclecaremicro.domain.port.in.CreateUserUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.UserRepositoryPort;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User createUser(User user) {
        if (userRepositoryPort.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email ya existe");
        }
        String normalizedName = normalizeName(user.getName());
        user.setName(normalizedName);
        if (user.getId() == null || user.getId().isBlank()) {
            user.setId(generateId());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepositoryPort.save(user);
    }

    private String generateId() {
        String id;
        do {
            id = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toLowerCase(Locale.ROOT);
        } while (userRepositoryPort.findById(id).isPresent());
        return id;
    }

    private String normalizeName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        String value = name.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (value.length() > 10) {
            throw new IllegalArgumentException("El nombre no puede superar 10 caracteres");
        }
        if (!value.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]+$")) {
            throw new IllegalArgumentException("El nombre solo puede contener letras y espacios");
        }
        return value;
    }
}
