package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.service.CatalogSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/catalog")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final CatalogSyncService catalogSyncService;

    @PostMapping("/sync")
    public ResponseEntity<Void> syncCatalog() {
        catalogSyncService.syncCatalog();
        return ResponseEntity.accepted().build();
    }
}
