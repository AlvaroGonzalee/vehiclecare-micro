package com.vehiclecare.vehiclecaremicro.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationContext {

    public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";

    public AuthenticatedUser requireCurrentUser(HttpServletRequest request) {
        Object value = request.getAttribute(AUTHENTICATED_USER_ATTRIBUTE);
        if (value instanceof AuthenticatedUser user) {
            return user;
        }
        throw new JwtAuthenticationException("No hay usuario autenticado en la petición");
    }
}
