package com.vehiclecare.vehiclecaremicro.domain.port.out;

import com.vehiclecare.vehiclecaremicro.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(String id);
    boolean existsByEmail(String email);
}
