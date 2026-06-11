package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vehiclecare.vehiclecaremicro.application.dto.response.AdminCatalogSyncResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.BrandResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.ModelResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.PageResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.CatalogQueryService;
import com.vehiclecare.vehiclecaremicro.application.service.CatalogSyncService;
import java.util.List;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class CatalogControllersTest {

    @Mock
    private CatalogQueryService catalogQueryService;
    @Mock
    private CatalogSyncService catalogSyncService;
    @InjectMocks
    private BrandController brandController;
    @InjectMocks
    private AdminCatalogController adminCatalogController;

    @Test
    void listBrands_capsPageSizeAtMaximum() {
        PageResponseDTO<BrandResponseDTO> page = new PageResponseDTO<>(List.of(new BrandResponseDTO("b1", "BMW", null)), 0, 100, 1);
        when(catalogQueryService.listBrands(org.mockito.ArgumentMatchers.eq("bmw"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(page);

        var response = brandController.listBrands("bmw", 0, 200);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(page, response.getBody());
    }

    @Test
    void listModels_capsPageSizeAtMaximum() {
        PageResponseDTO<ModelResponseDTO> page = new PageResponseDTO<>(List.of(new ModelResponseDTO("m1", "320i", "b1")), 0, 100, 1);
        when(catalogQueryService.listModels(org.mockito.ArgumentMatchers.eq("b1"), org.mockito.ArgumentMatchers.eq("320"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(page);

        var response = brandController.listModels("b1", "320", 0, 300);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(page, response.getBody());
    }

    @Test
    void syncCatalog_returnsDisabledResponseWhenConfigDisabled() {
        var response = adminCatalogController.syncCatalog();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AdminCatalogSyncResponseDTO body = response.getBody();
        assertFalse(body.isExecuted());
    }

    @Test
    void syncCatalog_executesWhenConfigEnabled() throws Exception {
        Field field = AdminCatalogController.class.getDeclaredField("catalogSyncEnabled");
        field.setAccessible(true);
        field.set(adminCatalogController, true);

        var response = adminCatalogController.syncCatalog();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().isExecuted());
        verify(catalogSyncService).syncCatalog();
    }
}
