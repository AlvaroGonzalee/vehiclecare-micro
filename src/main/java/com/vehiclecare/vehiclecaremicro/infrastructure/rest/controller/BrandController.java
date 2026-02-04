package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.dto.response.BrandResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.ModelResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.PageResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.CatalogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final CatalogQueryService catalogQueryService;

    @GetMapping
    public ResponseEntity<PageResponseDTO<BrandResponseDTO>> listBrands(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "page", required = false, defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(value = "size", required = false, defaultValue = "" + DEFAULT_SIZE) int size
    ) {
        int safeSize = Math.min(size, MAX_SIZE);
        PageRequest pageable = PageRequest.of(page, safeSize, Sort.by("name").ascending());
        return ResponseEntity.ok(catalogQueryService.listBrands(query, pageable));
    }

    @GetMapping("/{id}/models")
    public ResponseEntity<PageResponseDTO<ModelResponseDTO>> listModels(
            @PathVariable("id") String brandId,
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "page", required = false, defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(value = "size", required = false, defaultValue = "" + DEFAULT_SIZE) int size
    ) {
        int safeSize = Math.min(size, MAX_SIZE);
        PageRequest pageable = PageRequest.of(page, safeSize, Sort.by("name").ascending());
        return ResponseEntity.ok(catalogQueryService.listModels(brandId, query, pageable));
    }
}
