package com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception;

public class BusinessValidationException extends RuntimeException {

    public BusinessValidationException(String message) {
        super(message);
    }
}
