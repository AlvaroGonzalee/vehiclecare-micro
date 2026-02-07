package com.vehiclecare.vehiclecaremicro.domain.port.in;

import com.vehiclecare.vehiclecaremicro.domain.model.User;

public interface AuthenticateUserUseCase {
    User authenticate(String email, String password);
}
