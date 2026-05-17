package com.vehiclecare.vehiclecaremicro.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class AuthenticationContextTest {

    private AuthenticationContext authenticationContext;

    @BeforeEach
    void setUp() {
        authenticationContext = new AuthenticationContext();
    }

    @Test
    void requireCurrentUser_returnsAuthenticatedUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        AuthenticatedUser user = new AuthenticatedUser("user-1", "test@mail.com");
        request.setAttribute(AuthenticationContext.AUTHENTICATED_USER_ATTRIBUTE, user);

        AuthenticatedUser result = authenticationContext.requireCurrentUser(request);

        assertEquals(user, result);
    }

    @Test
    void requireCurrentUser_throwsWhenMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        JwtAuthenticationException ex = assertThrows(
                JwtAuthenticationException.class,
                () -> authenticationContext.requireCurrentUser(request)
        );

        assertEquals("No hay usuario autenticado en la petición", ex.getMessage());
    }
}
