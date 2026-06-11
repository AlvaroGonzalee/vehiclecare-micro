package com.vehiclecare.vehiclecaremicro.infrastructure.security;

import com.vehiclecare.vehiclecaremicro.application.service.JwtService;
import com.vehiclecare.vehiclecaremicro.domain.port.out.UserRepositoryPort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servlet interceptor that enforces JWT-based authentication for protected requests.
 *
 * <p>The interceptor extracts the Bearer token from the {@code Authorization} header,
 * validates it through {@link JwtService}, verifies that the referenced user still
 * exists and stores the authenticated principal in the current request for later
 * retrieval by controllers and other infrastructure components.</p>
 */
@Component
@Slf4j
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final UserRepositoryPort userRepositoryPort;

    public JwtAuthenticationInterceptor(JwtService jwtService, UserRepositoryPort userRepositoryPort) {
        this.jwtService = jwtService;
        this.userRepositoryPort = userRepositoryPort;
    }

    /**
     * Authenticates the current request before it reaches the controller layer.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler selected handler
     * @return {@code true} when the request is authenticated, {@code false} otherwise
     * @throws IOException if the unauthorized response cannot be written
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        log.debug("Authenticating request method={} path={}", request.getMethod(), request.getRequestURI());
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("Missing or invalid Bearer token method={} path={}", request.getMethod(), request.getRequestURI());
            writeUnauthorized(request, response, "Falta el token Bearer");
            return false;
        }

        String token = authorization.substring(7).trim();
        if (token.isBlank()) {
            log.warn("Empty Bearer token method={} path={}", request.getMethod(), request.getRequestURI());
            writeUnauthorized(request, response, "El token Bearer está vacío");
            return false;
        }

        try {
            JwtService.JwtClaims claims = jwtService.validateUserToken(token);
            boolean userExists = userRepositoryPort.findById(claims.userId()).isPresent();
            if (!userExists) {
                throw new JwtAuthenticationException("El usuario del token ya no existe");
            }
            log.info("Request authenticated method={} path={} userId={}", request.getMethod(), request.getRequestURI(), claims.userId());
            request.setAttribute(
                    AuthenticationContext.AUTHENTICATED_USER_ATTRIBUTE,
                    new AuthenticatedUser(claims.userId(), claims.email())
            );
            return true;
        } catch (JwtAuthenticationException ex) {
            log.warn("JWT authentication failed method={} path={} reason={}",
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
