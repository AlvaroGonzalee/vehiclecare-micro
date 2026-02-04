package com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository;

import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.ModelEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModelJpaRepository extends JpaRepository<ModelEntity, String> {
    Page<ModelEntity> findByActiveTrueAndBrand_Id(String brandId, Pageable pageable);
    Page<ModelEntity> findByActiveTrueAndBrand_IdAndNameContainingIgnoreCase(String brandId, String name, Pageable pageable);
    List<ModelEntity> findByBrand_Id(String brandId);
}
