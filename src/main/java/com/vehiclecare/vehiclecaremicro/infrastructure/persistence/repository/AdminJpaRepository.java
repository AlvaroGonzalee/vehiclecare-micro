package com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository;

import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.AdminEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminJpaRepository extends JpaRepository<AdminEntity, String> {
    Optional<AdminEntity> findByEmail(String email);
}
