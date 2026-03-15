package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.dto.request.UserProfileUpdateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.request.UserRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.FileUploadResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.UserResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.MinioStorageService;
import com.vehiclecare.vehiclecaremicro.domain.model.User;
import com.vehiclecare.vehiclecaremicro.domain.port.in.CreateUserUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.UserRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final UserRepositoryPort userRepositoryPort;
    private final MinioStorageService minioStorageService;
    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO requestDTO) {
        User user = userMapper.toDomain(requestDTO);
        User created = createUserUseCase.createUser(user);
        return new ResponseEntity<>(userMapper.toResponse(created), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable("id") String id) {
        return userRepositoryPort.findById(id)
                .map(userMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @PathVariable("id") String id,
            @Valid @RequestBody UserProfileUpdateRequestDTO requestDTO
    ) {
        User existing = userRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        existing.setName(requestDTO.getName().trim());
        User saved = userRepositoryPort.save(existing);
        return ResponseEntity.ok(userMapper.toResponse(saved));
    }

    @PostMapping("/{id}/profile-image")
    public ResponseEntity<UserResponseDTO> uploadProfileImage(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile file
    ) {
        User existing = userRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        FileUploadResponseDTO upload = minioStorageService.uploadImage(file, "profiles/" + id);
        existing.setProfileImageUrl(upload.getObjectUrl());
        User saved = userRepositoryPort.save(existing);
        return ResponseEntity.ok(userMapper.toResponse(saved));
    }
}
