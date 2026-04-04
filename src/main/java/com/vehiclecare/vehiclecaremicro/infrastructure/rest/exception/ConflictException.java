package com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
