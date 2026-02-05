package com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String path;
    private List<ApiFieldError> details;
}
