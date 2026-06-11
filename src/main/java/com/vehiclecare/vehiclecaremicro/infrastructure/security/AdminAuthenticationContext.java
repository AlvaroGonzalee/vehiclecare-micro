package com.vehiclecare.vehiclecaremicro.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class AdminAuthenticationContext {

    public static final String AUTHENTICATED_ADMIN_ATTRIBUTE = "authenticatedAdmin";

    public AdminAuthenticatedUser requireCurrentAdmin(HttpServletRequest request) {
        Object value = request.getAttribute(AUTHENTICATED_ADMIN_ATTRIBUTE);
        if (value instanceof AdminAuthenticatedUser admin) {
            return admin;
        }
        throw new JwtAuthenticationException("No hay administrador autenticado en la petición");
    }
}
