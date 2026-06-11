package com.vehiclecare.vehiclecaremicro.application.service;

import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.UserEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.AttachmentJpaRepository;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.MaintenanceRecordJpaRepository;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.UserJpaRepository;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.VehicleJpaRepository;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserManagementService {

    private final UserJpaRepository userJpaRepository;
    private final VehicleJpaRepository vehicleJpaRepository;
    private final MaintenanceRecordJpaRepository maintenanceRecordJpaRepository;
    private final AttachmentJpaRepository attachmentJpaRepository;
    private final MinioStorageService minioStorageService;

    @Transactional
    public void deleteUser(String userId) {
        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        log.info("Deleting user from admin panel userId={}", userId);
        minioStorageService.delete(user.getProfileImageUrl());

        vehicleJpaRepository.findImageUrlsByUserId(userId).stream()
                .filter(path -> path != null && !path.isBlank())
                .forEach(minioStorageService::delete);

        attachmentJpaRepository.findFilePathsByUserId(userId).stream()
                .filter(path -> path != null && !path.isBlank())
                .forEach(minioStorageService::delete);

        userJpaRepository.delete(user);
        log.info("User deleted from admin panel userId={}", userId);
    }
}
