package com.vehiclecare.vehiclecaremicro.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vehiclecare.vehiclecaremicro.application.dto.response.BrandResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.ModelResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.PageResponseDTO;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.BrandMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.ModelMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.BrandEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.ModelEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.BrandJpaRepository;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.ModelJpaRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class CatalogQueryServiceTest {

    @Mock
    private BrandJpaRepository brandJpaRepository;
    @Mock
    private ModelJpaRepository modelJpaRepository;
    @Mock
    private BrandMapper brandMapper;
    @Mock
    private ModelMapper modelMapper;

    private CatalogQueryService service;

    @BeforeEach
    void setUp() {
        service = new CatalogQueryService(brandJpaRepository, modelJpaRepository, brandMapper, modelMapper);
    }

    @Test
    void listBrands_usesActiveQueryWhenSearchBlank() {
        PageRequest pageable = PageRequest.of(1, 5);
        BrandEntity brand = BrandEntity.builder().id("1").name("BMW").active(true).build();
        BrandResponseDTO dto = new BrandResponseDTO("1", "BMW", null);
        Page<BrandEntity> page = new PageImpl<>(List.of(brand), pageable, 6);
        when(brandJpaRepository.findByActiveTrue(pageable)).thenReturn(page);
        when(brandMapper.toResponse(brand)).thenReturn(dto);

        PageResponseDTO<BrandResponseDTO> response = service.listBrands("   ", pageable);

        assertEquals(List.of(dto), response.getItems());
        assertEquals(1, response.getPage());
        assertEquals(5, response.getSize());
        assertEquals(6, response.getTotal());
    }

    @Test
    void listBrands_normalizesSearchQuery() {
        PageRequest pageable = PageRequest.of(0, 10);
        BrandEntity brand = BrandEntity.builder().id("1").name("BMW").active(true).build();
        BrandResponseDTO dto = new BrandResponseDTO("1", "BMW", null);
        Page<BrandEntity> page = new PageImpl<>(List.of(brand), pageable, 1);
        when(brandJpaRepository.findByActiveTrueAndNameContainingIgnoreCase("bmw", pageable)).thenReturn(page);
        when(brandMapper.toResponse(brand)).thenReturn(dto);

        PageResponseDTO<BrandResponseDTO> response = service.listBrands("  BmW  ", pageable);

        assertEquals(List.of(dto), response.getItems());
        verify(brandJpaRepository).findByActiveTrueAndNameContainingIgnoreCase("bmw", pageable);
    }

    @Test
    void listModels_usesActiveQueryWhenSearchBlank() {
        PageRequest pageable = PageRequest.of(0, 5);
        ModelEntity model = ModelEntity.builder().id("m1").name("320i").active(true).build();
        ModelResponseDTO dto = new ModelResponseDTO("m1", "320i", "b1");
        Page<ModelEntity> page = new PageImpl<>(List.of(model), pageable, 1);
        when(modelJpaRepository.findByActiveTrueAndBrand_Id("b1", pageable)).thenReturn(page);
        when(modelMapper.toResponse(model)).thenReturn(dto);

        PageResponseDTO<ModelResponseDTO> response = service.listModels("b1", null, pageable);

        assertEquals(List.of(dto), response.getItems());
        assertEquals(0, response.getPage());
        assertEquals(5, response.getSize());
        assertEquals(1, response.getTotal());
    }

    @Test
    void listModels_normalizesSearchQuery() {
        PageRequest pageable = PageRequest.of(2, 3);
        ModelEntity model = ModelEntity.builder().id("m1").name("320i").active(true).build();
        ModelResponseDTO dto = new ModelResponseDTO("m1", "320i", "b1");
        Page<ModelEntity> page = new PageImpl<>(List.of(model), pageable, 9);
        when(modelJpaRepository.findByActiveTrueAndBrand_IdAndNameContainingIgnoreCase("b1", "series", pageable))
                .thenReturn(page);
        when(modelMapper.toResponse(model)).thenReturn(dto);

        PageResponseDTO<ModelResponseDTO> response = service.listModels("b1", "  SeRies  ", pageable);

        assertSame(dto, response.getItems().get(0));
        verify(modelJpaRepository).findByActiveTrueAndBrand_IdAndNameContainingIgnoreCase("b1", "series", pageable);
    }
}
