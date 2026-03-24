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
import java.util.Locale;
import java.text.Normalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class CatalogSyncService {

    private static final Logger logger = LoggerFactory.getLogger(CatalogSyncService.class);
    private static final ReentrantLock SYNC_LOCK = new ReentrantLock();
    private static final Set<String> ALLOWED_BRANDS = Set.of(
            "abarth",
            "acura",
            "alfa romeo",
            "audi",
            "bentley",
            "bmw",
            "buick",
            "bugatti",
            "cadillac",
            "chery",
            "chevrolet",
            "chrysler",
            "citroen",
            "cupra",
            "dacia",
            "daewoo",
            "daihatsu",
            "dodge",
            "ds automobiles",
            "ds",
            "ferrari",
            "fiat",
            "ford",
            "genesis",
            "gmc",
            "great wall",
            "honda",
            "hummer",
            "hyundai",
            "infiniti",
            "isuzu",
            "jaguar",
            "jeep",
            "kia",
            "koenigsegg",
            "lamborghini",
            "lancia",
            "land rover",
            "lexus",
            "lotus",
            "maserati",
            "mazda",
            "mercedes benz",
            "mini",
            "mitsubishi",
            "mg",
            "nissan",
            "opel",
            "pagani",
            "peugeot",
            "porsche",
            "polestar",
            "renault",
            "rolls royce",
            "rover",
            "saab",
            "seat",
            "skoda",
            "smart",
            "ssangyong",
            "subaru",
            "suzuki",
            "tata",
            "tesla",
            "toyota",
            "volkswagen",
            "volvo",
            "byd"
    );

    private final VpicClient vpicClient;
    private final BrandJpaRepository brandJpaRepository;
    private final ModelJpaRepository modelJpaRepository;

    @Value("${catalog.sync.enabled:false}")
    private boolean catalogSyncEnabled;

    public void syncCatalog() {
        if (!SYNC_LOCK.tryLock()) {
            logger.warn("Catalog sync skipped because another sync is already running");
            return;
        }
        try {
        List<VpicMakeDTO> makes = vpicClient.fetchMakes();
        if (makes.isEmpty()) {
            return;
        }
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
            if (!isAllowedBrand(make.getName())) {
                continue;
            }

            BrandEntity brand = brandById.getOrDefault(make.getId(), BrandEntity.builder()
                    .id(make.getId())
                    .build());

            brand.setName(make.getName().trim());
            brand.setActive(true);
            brand = brandJpaRepository.save(brand);

            activeBrandIds.add(brand.getId());

            try {
                syncModelsForBrand(brand, make.getId());
            } catch (PessimisticLockingFailureException ex) {
                logger.warn("Lock timeout syncing models for brand {}. Skipping.", brand.getId());
            }
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
        } finally {
            SYNC_LOCK.unlock();
        }
    }

    @Scheduled(fixedDelayString = "PT720H")
    @Transactional
    public void scheduledSync() {
        if (!catalogSyncEnabled) {
            logger.info("Catalog sync is disabled. Skipping scheduled sync.");
            return;
        }
        syncCatalog();
    }

    @Transactional
    protected void syncModelsForBrand(BrandEntity brand, String makeId) {
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

    private boolean isAllowedBrand(String name) {
        String normalized = normalize(name);
        if (ALLOWED_BRANDS.contains(normalized)) {
            return true;
        }
        for (String allowed : ALLOWED_BRANDS) {
            if (normalized.startsWith(allowed + " ")) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        normalized = normalized
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
        return normalized;
    }
}
