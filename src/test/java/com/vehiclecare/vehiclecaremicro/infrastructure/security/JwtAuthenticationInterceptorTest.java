package com.vehiclecare.vehiclecaremicro.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.vehiclecare.vehiclecaremicro.application.service.JwtService;
import com.vehiclecare.vehiclecaremicro.domain.model.User;
import com.vehiclecare.vehiclecaremicro.domain.port.out.UserRepositoryPort;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationInterceptorTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    private JwtAuthenticationInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new JwtAuthenticationInterceptor(jwtService, userRepositoryPort);
    }

    @Test
    void preHandle_allowsOptionsRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/vehicles");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    void preHandle_rejectsWhenAuthorizationHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/vehicles");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandle_rejectsWhenAuthorizationHeaderIsNotBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/vehicles");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandle_rejectsWhenTokenBlank() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/vehicles");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer   ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandle_rejectsWhenJwtValidationFails() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/vehicles");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtService.validateToken("token")).thenThrow(new JwtAuthenticationException("Token JWT inválido"));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandle_rejectsWhenUserNoLongerExists() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/vehicles");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtService.validateToken("token")).thenReturn(new JwtService.JwtClaims("user-1", "a@a.com", 999999L));
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.empty());

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(401, response.getStatus());
    }

    @Test
    void preHandle_setsAuthenticatedUserWhenTokenValid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/vehicles");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtService.validateToken("token")).thenReturn(new JwtService.JwtClaims("user-1", "a@a.com", 999999L));
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(new User()));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        Object attribute = request.getAttribute(AuthenticationContext.AUTHENTICATED_USER_ATTRIBUTE);
        assertTrue(attribute instanceof AuthenticatedUser);
    }

    private static void assertEquals(int expected, int actual) {
        org.junit.jupiter.api.Assertions.assertEquals(expected, actual);
    }
}
