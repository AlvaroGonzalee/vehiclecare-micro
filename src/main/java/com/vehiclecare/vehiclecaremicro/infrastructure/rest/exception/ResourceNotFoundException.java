package com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
