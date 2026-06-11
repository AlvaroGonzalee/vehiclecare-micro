package com.vehiclecare.vehiclecaremicro.application.service;

import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.AttachmentEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.MaintenanceRecordEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.UserEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.entity.VehicleEntity;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.MaintenanceRecordJpaRepository;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.UserJpaRepository;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.VehicleJpaRepository;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.ResourceNotFoundException;
import java.util.List;
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
    private final MinioStorageService minioStorageService;

    @Transactional
    public void deleteUser(String userId) {
        UserEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        log.info("Deleting user from admin panel userId={}", userId);
        minioStorageService.delete(user.getProfileImageUrl());

        List<VehicleEntity> vehicles = vehicleJpaRepository.findByUser_IdOrderByIdDesc(userId);
        for (VehicleEntity vehicle : vehicles) {
            minioStorageService.delete(vehicle.getImageUrl());
        }

        List<MaintenanceRecordEntity> records = maintenanceRecordJpaRepository
                .findByVehicle_User_IdOrderByMaintenanceDateDesc(userId);
        for (MaintenanceRecordEntity record : records) {
            for (AttachmentEntity attachment : record.getAttachments()) {
                minioStorageService.delete(attachment.getFilePath());
            }
        }

        userJpaRepository.delete(user);
        log.info("User deleted from admin panel userId={}", userId);
    }
}
