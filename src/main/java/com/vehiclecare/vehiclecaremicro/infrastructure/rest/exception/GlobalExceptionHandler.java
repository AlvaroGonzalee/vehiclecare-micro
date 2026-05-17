package com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Request validation failed method={} path={} errorsCount={}",
                request.getMethod(), request.getRequestURI(), ex.getBindingResult().getFieldErrorCount());
        List<ApiFieldError> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .collect(Collectors.toList());

        return buildResponse(HttpStatus.BAD_REQUEST, "Validación incorrecta", request.getRequestURI(), details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        log.warn("Constraint validation failed method={} path={} errorsCount={}",
                request.getMethod(), request.getRequestURI(), ex.getConstraintViolations().size());
        List<ApiFieldError> details = ex.getConstraintViolations()
                .stream()
                .map(violation -> new ApiFieldError(violation.getPropertyPath().toString(), violation.getMessage()))
                .collect(Collectors.toList());

        return buildResponse(HttpStatus.BAD_REQUEST, "Validación incorrecta", request.getRequestURI(), details);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument method={} path={} message={}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        ApiFieldError detail = new ApiFieldError("general", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Solicitud inválida", request.getRequestURI(), List.of(detail));
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessValidation(
            BusinessValidationException ex,
            HttpServletRequest request
    ) {
        log.warn("Business validation failed method={} path={} message={}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        ApiFieldError detail = new ApiFieldError("general", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Validación de negocio", request.getRequestURI(), List.of(detail));
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationFailed(
            AuthenticationFailedException ex,
            HttpServletRequest request
    ) {
        log.warn("Authentication failed method={} path={} message={}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        ApiFieldError detail = new ApiFieldError("general", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "No autenticado", request.getRequestURI(), List.of(detail));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Resource not found method={} path={} message={}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        ApiFieldError detail = new ApiFieldError("general", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso no encontrado", request.getRequestURI(), List.of(detail));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest request
    ) {
        log.warn("Conflict detected method={} path={} message={}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        ApiFieldError detail = new ApiFieldError("general", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "Conflicto", request.getRequestURI(), List.of(detail));
    }

    @ExceptionHandler(OwnershipAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleOwnershipAccess(OwnershipAccessException ex, HttpServletRequest request) {
        log.warn("Ownership access denied method={} path={} message={}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        ApiFieldError detail = new ApiFieldError("general", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "Acceso denegado", request.getRequestURI(), List.of(detail));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Invalid JSON received method={} path={}", request.getMethod(), request.getRequestURI());
        ApiFieldError detail = new ApiFieldError("body", "JSON inválido");
        return buildResponse(HttpStatus.BAD_REQUEST, "Solicitud inválida", request.getRequestURI(), List.of(detail));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected server error method={} path={}", request.getMethod(), request.getRequestURI(), ex);
        ApiFieldError detail = new ApiFieldError("general", "Error interno del servidor");
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno",
                request.getRequestURI(),
                List.of(detail)
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            String path,
            List<ApiFieldError> details
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                error,
                path,
                details
        );
        return ResponseEntity.status(status).body(response);
    }

    private ApiFieldError toFieldError(FieldError error) {
        return new ApiFieldError(error.getField(), error.getDefaultMessage());
    }
}
