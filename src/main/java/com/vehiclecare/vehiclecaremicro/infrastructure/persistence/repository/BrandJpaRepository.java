package com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository;

import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.BrandEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandJpaRepository extends JpaRepository<BrandEntity, String> {
    Page<BrandEntity> findByActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
    Page<BrandEntity> findByActiveTrue(Pageable pageable);
}
