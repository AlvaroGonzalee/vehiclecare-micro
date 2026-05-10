package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.dto.request.UserProfileUpdateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.FileUploadResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.UserResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.MinioStorageService;
import com.vehiclecare.vehiclecaremicro.application.service.PublicFileUrlService;
import com.vehiclecare.vehiclecaremicro.domain.model.User;
import com.vehiclecare.vehiclecaremicro.domain.port.out.UserRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.UserMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.OwnershipAccessException;
import com.vehiclecare.vehiclecaremicro.infrastructure.security.AuthenticationContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
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

    private final UserRepositoryPort userRepositoryPort;
    private final MinioStorageService minioStorageService;
    private final PublicFileUrlService publicFileUrlService;
    private final UserMapper userMapper;
    private final AuthenticationContext authenticationContext;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable("id") String id, HttpServletRequest request) {
        ensureOwnership(id, request);
        return userRepositoryPort.findById(id)
                .map(user -> toResponse(user, request))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @PathVariable("id") String id,
            @Valid @RequestBody UserProfileUpdateRequestDTO requestDTO,
            HttpServletRequest request
    ) {
        ensureOwnership(id, request);
        User existing = userRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        existing.setName(requestDTO.getName().trim());
        User saved = userRepositoryPort.save(existing);
        return ResponseEntity.ok(toResponse(saved, request));
    }

    @PostMapping("/{id}/profile-image")
    public ResponseEntity<UserResponseDTO> uploadProfileImage(
            @PathVariable("id") String id,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) {
        ensureOwnership(id, request);
        User existing = userRepositoryPort.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        FileUploadResponseDTO upload = minioStorageService.uploadImage(file, "profiles/" + id);
        existing.setProfileImageUrl(upload.getObjectKey());
        User saved = userRepositoryPort.save(existing);
        return ResponseEntity.ok(toResponse(saved, request));
    }

    private UserResponseDTO toResponse(User user, HttpServletRequest request) {
        UserResponseDTO response = userMapper.toResponse(user);
        response.setProfileImageUrl(
                publicFileUrlService.buildObjectUrl(request, response.getProfileImageUrl())
        );
        return response;
    }

    private void ensureOwnership(String requestedUserId, HttpServletRequest request) {
        String authenticatedUserId = authenticationContext.requireCurrentUser(request).userId();
        if (!authenticatedUserId.equals(requestedUserId)) {
            throw new OwnershipAccessException("No puedes acceder al perfil de otro usuario");
        }
    }
}
