package com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiFieldError {
    private String field;
    private String message;
}
