package com.vehiclecare.vehiclecaremicro.application.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageResponseDTO<T> {
    private List<T> items;
    private int page;
    private int size;
    private long total;
}
