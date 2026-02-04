package com.vehiclecare.vehiclecaremicro.application.service;

import com.vehiclecare.vehiclecaremicro.infrastructure.external.vpic.VpicClient;
import com.vehiclecare.vehiclecaremicro.infrastructure.external.vpic.VpicMakeDTO;
import com.vehiclecare.vehiclecaremicro.infrastructure.external.vpic.VpicModelDTO;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.BrandEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.ModelEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.BrandJpaRepository;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.ModelJpaRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogSyncService {

    private final VpicClient vpicClient;
    private final BrandJpaRepository brandJpaRepository;
    private final ModelJpaRepository modelJpaRepository;

    @Transactional
    public void syncCatalog() {
        List<VpicMakeDTO> makes = vpicClient.fetchMakes();
        List<BrandEntity> existingBrands = brandJpaRepository.findAll();
        Map<String, BrandEntity> brandById = new HashMap<>();
        for (BrandEntity brand : existingBrands) {
            brandById.put(brand.getId(), brand);
        }

        Set<String> activeBrandIds = new HashSet<>();

        for (VpicMakeDTO make : makes) {
            if (make.getId() == null || make.getName() == null) {
                continue;
            }

            BrandEntity brand = brandById.getOrDefault(make.getId(), BrandEntity.builder()
                    .id(make.getId())
                    .build());

            brand.setName(make.getName().trim());
            brand.setActive(true);
            brand = brandJpaRepository.save(brand);

            activeBrandIds.add(brand.getId());

            syncModelsForBrand(brand, make.getId());
        }

        for (BrandEntity brand : existingBrands) {
            if (!activeBrandIds.contains(brand.getId())) {
                brand.setActive(false);
                brandJpaRepository.save(brand);

                List<ModelEntity> models = modelJpaRepository.findByBrand_Id(brand.getId());
                for (ModelEntity model : models) {
                    model.setActive(false);
                }
                modelJpaRepository.saveAll(models);
            }
        }
    }

    @Scheduled(fixedDelayString = "PT720H")
    @ConditionalOnProperty(name = "catalog.sync.enabled", havingValue = "true", matchIfMissing = true)
    @Transactional
    public void scheduledSync() {
        syncCatalog();
    }

    private void syncModelsForBrand(BrandEntity brand, String makeId) {
        List<ModelEntity> existingModels = modelJpaRepository.findByBrand_Id(brand.getId());
        Map<String, ModelEntity> modelById = new HashMap<>();
        for (ModelEntity model : existingModels) {
            modelById.put(model.getId(), model);
        }

        List<VpicModelDTO> models = vpicClient.fetchModelsForMake(makeId);
        Set<String> activeModelIds = new HashSet<>();

        for (VpicModelDTO modelDto : models) {
            if (modelDto.getId() == null || modelDto.getName() == null) {
                continue;
            }

            ModelEntity model = modelById.getOrDefault(modelDto.getId(), ModelEntity.builder()
                    .id(modelDto.getId())
                    .brand(brand)
                    .build());

            model.setName(modelDto.getName().trim());
            model.setActive(true);
            model.setBrand(brand);
            modelJpaRepository.save(model);

            activeModelIds.add(model.getId());
        }

        for (ModelEntity model : existingModels) {
            if (!activeModelIds.contains(model.getId())) {
                model.setActive(false);
            }
        }

        modelJpaRepository.saveAll(existingModels);
    }
}
