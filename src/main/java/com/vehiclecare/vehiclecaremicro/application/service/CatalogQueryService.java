package com.vehiclecare.vehiclecaremicro.application.service;

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
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogQueryService {

    private final BrandJpaRepository brandJpaRepository;
    private final ModelJpaRepository modelJpaRepository;
    private final BrandMapper brandMapper;
    private final ModelMapper modelMapper;

    public PageResponseDTO<BrandResponseDTO> listBrands(String query, Pageable pageable) {
        Page<BrandEntity> page = isBlank(query)
                ? brandJpaRepository.findByActiveTrue(pageable)
                : brandJpaRepository.findByActiveTrueAndNameContainingIgnoreCase(normalize(query), pageable);

        List<BrandResponseDTO> items = page.getContent()
                .stream()
                .map(brandMapper::toResponse)
                .toList();

        return new PageResponseDTO<>(items, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    public PageResponseDTO<ModelResponseDTO> listModels(String brandId, String query, Pageable pageable) {
        Page<ModelEntity> page = isBlank(query)
                ? modelJpaRepository.findByActiveTrueAndBrand_Id(brandId, pageable)
                : modelJpaRepository.findByActiveTrueAndBrand_IdAndNameContainingIgnoreCase(
                        brandId,
                        normalize(query),
                        pageable
                );

        List<ModelResponseDTO> items = page.getContent()
                .stream()
                .map(modelMapper::toResponse)
                .toList();

        return new PageResponseDTO<>(items, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
