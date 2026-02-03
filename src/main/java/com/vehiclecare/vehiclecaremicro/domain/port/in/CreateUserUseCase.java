package com.vehiclecare.vehiclecaremicro.domain.port.in;

import com.vehiclecare.vehiclecaremicro.domain.model.User;

public interface CreateUserUseCase {
    User createUser(User user);
}
