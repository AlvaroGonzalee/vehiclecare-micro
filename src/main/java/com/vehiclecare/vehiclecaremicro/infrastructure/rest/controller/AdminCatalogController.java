package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.dto.response.AdminCatalogSyncResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.CatalogSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/catalog")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final CatalogSyncService catalogSyncService;
    @Value("${catalog.sync.enabled:false}")
    private boolean catalogSyncEnabled;

    @PostMapping("/sync")
    public ResponseEntity<AdminCatalogSyncResponseDTO> syncCatalog() {
        if (!catalogSyncEnabled) {
            return ResponseEntity.ok(
                    new AdminCatalogSyncResponseDTO(
                            false,
                            false,
                            "La sincronización está desactivada por configuración"
                    )
            );
        }
        catalogSyncService.syncCatalog();
        return ResponseEntity.ok(
                new AdminCatalogSyncResponseDTO(
                        true,
                        true,
                        "Sincronización lanzada correctamente"
                )
        );
    }
}
