package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.application.service.ValidationService;
import com.vehiclecare.vehiclecaremicro.domain.model.User;
import com.vehiclecare.vehiclecaremicro.domain.port.in.CreateUserUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.UserRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.ConflictException;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateUserUseCaseImpl implements CreateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final ValidationService validationService;

    @Override
    @Transactional
    public User createUser(User user) {
        log.info("Creating user email={}", user.getEmail());
        validationService.normalizeAndValidateUser(user);
        if (userRepositoryPort.existsByEmail(user.getEmail())) {
            log.warn("User creation rejected because email already exists email={}", user.getEmail());
            throw new ConflictException("Ya existe un usuario con ese email");
        }
        if (user.getId() == null || user.getId().isBlank()) {
            user.setId(generateId());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User saved = userRepositoryPort.save(user);
        log.info("User created successfully userId={} email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    private String generateId() {
        String id;
        do {
            id = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toLowerCase(Locale.ROOT);
        } while (userRepositoryPort.findById(id).isPresent());
        return id;
    }

}
