package com.vehiclecare.vehiclecaremicro.infrastructure.security;

import com.vehiclecare.vehiclecaremicro.application.service.JwtService;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.AdminJpaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class AdminJwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final AdminJpaRepository adminJpaRepository;

    public AdminJwtAuthenticationInterceptor(
            JwtService jwtService,
            AdminJpaRepository adminJpaRepository
    ) {
        this.jwtService = jwtService;
        this.adminJpaRepository = adminJpaRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        log.debug("Authenticating admin request method={} path={}", request.getMethod(), request.getRequestURI());
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(request, response, "Falta el token Bearer de administrador");
            return false;
        }

        String token = authorization.substring(7).trim();
        if (token.isBlank()) {
            writeUnauthorized(request, response, "El token Bearer de administrador está vacío");
            return false;
        }

        try {
            JwtService.JwtClaims claims = jwtService.validateAdminToken(token);
            boolean adminExists = adminJpaRepository.findById(claims.userId()).isPresent();
            if (!adminExists) {
                throw new JwtAuthenticationException("El administrador del token ya no existe");
            }
            request.setAttribute(
                    AdminAuthenticationContext.AUTHENTICATED_ADMIN_ATTRIBUTE,
                    new AdminAuthenticatedUser(claims.userId(), claims.email())
            );
            return true;
        } catch (JwtAuthenticationException ex) {
            log.warn("Admin JWT authentication failed method={} path={} reason={}",
                    request.getMethod(), request.getRequestURI(), ex.getMessage());
            writeUnauthorized(request, response, ex.getMessage());
            return false;
        }
    }

    private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = """
                {"timestamp":"%s","status":401,"error":"Unauthorized","path":"%s","details":[{"field":"authorization","message":"%s"}]}
                """.formatted(Instant.now(), escape(request.getRequestURI()), escape(message));
        response.getWriter().write(body);
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
