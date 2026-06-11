package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.dto.response.UserResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.AdminUserManagementService;
import com.vehiclecare.vehiclecaremicro.application.service.PublicFileUrlService;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.UserMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.persistence.repository.UserJpaRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;
    private final PublicFileUrlService publicFileUrlService;
    private final AdminUserManagementService adminUserManagementService;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> listUsers(
            @RequestParam(value = "q", required = false) String query,
            HttpServletRequest request
    ) {
        String normalizedQuery = query == null ? null : query.trim().toLowerCase();
        List<UserResponseDTO> users = userJpaRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(userMapper::toDomain)
                .filter(user -> matchesUserQuery(user.getId(), user.getName(), user.getEmail(), normalizedQuery))
                .map(userMapper::toResponse)
                .map(user -> toResponse(user, request))
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable("id") String id, HttpServletRequest request) {
        return userJpaRepository.findById(id)
                .map(userMapper::toDomain)
                .map(userMapper::toResponse)
                .map(user -> toResponse(user, request))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") String id) {
        adminUserManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private UserResponseDTO toResponse(UserResponseDTO user, HttpServletRequest request) {
        user.setProfileImageUrl(publicFileUrlService.buildObjectUrl(request, user.getProfileImageUrl()));
        return user;
    }

    private boolean matchesUserQuery(String id, String name, String email, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        return containsIgnoreCase(id, query)
                || containsIgnoreCase(name, query)
                || containsIgnoreCase(email, query);
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }
}
