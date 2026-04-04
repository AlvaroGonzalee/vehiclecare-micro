package com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception;

public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
