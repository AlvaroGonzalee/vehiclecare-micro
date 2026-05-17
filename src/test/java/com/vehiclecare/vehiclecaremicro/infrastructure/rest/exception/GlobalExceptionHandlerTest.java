package com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleValidation_returnsBadRequest() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "name", "El nombre es obligatorio"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        MockHttpServletRequest request = request();

        var response = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("name", response.getBody().getDetails().get(0).getField());
    }

    @Test
    void handleConstraintViolation_returnsBadRequest() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("price");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("No válido");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        var response = handler.handleConstraintViolation(ex, request());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("price", response.getBody().getDetails().get(0).getField());
    }

    @Test
    void handleIllegalArgument_returnsBadRequest() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("bad"), request());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleBusinessValidation_returnsBadRequest() {
        var response = handler.handleBusinessValidation(new BusinessValidationException("bad"), request());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleAuthenticationFailed_returnsUnauthorized() {
        var response = handler.handleAuthenticationFailed(new AuthenticationFailedException("bad"), request());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void handleResourceNotFound_returnsNotFound() {
        var response = handler.handleResourceNotFound(new ResourceNotFoundException("bad"), request());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleConflict_returnsConflict() {
        var response = handler.handleConflict(new ConflictException("bad"), request());
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void handleOwnershipAccess_returnsForbidden() {
        var response = handler.handleOwnershipAccess(new OwnershipAccessException("bad"), request());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleInvalidJson_returnsBadRequest() {
        var response = handler.handleInvalidJson(new HttpMessageNotReadableException("bad"), request());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("body", response.getBody().getDetails().get(0).getField());
    }

    @Test
    void handleUnexpectedException_returnsInternalServerError() {
        var response = handler.handleUnexpectedException(new RuntimeException("boom"), request());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/test");
        return request;
    }
}
