package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vehiclecare.vehiclecaremicro.application.dto.request.UserProfileUpdateRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.FileUploadResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.UserResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.MinioStorageService;
import com.vehiclecare.vehiclecaremicro.application.service.PublicFileUrlService;
import com.vehiclecare.vehiclecaremicro.domain.model.User;
import com.vehiclecare.vehiclecaremicro.domain.port.out.UserRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.mapper.UserMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.OwnershipAccessException;
import com.vehiclecare.vehiclecaremicro.infrastructure.security.AuthenticatedUser;
import com.vehiclecare.vehiclecaremicro.infrastructure.security.AuthenticationContext;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private MinioStorageService minioStorageService;
    @Mock
    private PublicFileUrlService publicFileUrlService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AuthenticationContext authenticationContext;

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(userRepositoryPort, minioStorageService, publicFileUrlService, userMapper, authenticationContext);
    }

    @Test
    void getById_returnsProfileWhenOwned() {
        MockHttpServletRequest request = request("user-1");
        User user = user();
        UserResponseDTO responseDTO = userResponse();
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(responseDTO);
        when(publicFileUrlService.buildObjectUrl(request, "profiles/1.jpg")).thenReturn("http://public");

        var response = controller.getById("user-1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("http://public", response.getBody().getProfileImageUrl());
    }

    @Test
    void getById_returnsNotFoundWhenMissing() {
        MockHttpServletRequest request = request("user-1");
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.empty());

        var response = controller.getById("user-1", request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateProfile_updatesName() {
        MockHttpServletRequest request = request("user-1");
        User user = user();
        UserResponseDTO responseDTO = userResponse();
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(user));
        when(userRepositoryPort.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(responseDTO);
        when(publicFileUrlService.buildObjectUrl(request, "profiles/1.jpg")).thenReturn("http://public");

        var response = controller.updateProfile("user-1", new UserProfileUpdateRequestDTO(" Nuevo "), request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Nuevo", user.getName());
    }

    @Test
    void updateProfile_throwsWhenUserMissing() {
        MockHttpServletRequest request = request("user-1");
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.updateProfile("user-1", new UserProfileUpdateRequestDTO(" Nuevo "), request)
        );

        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    @Test
    void uploadProfileImage_uploadsAndSaves() {
        MockHttpServletRequest request = request("user-1");
        MockMultipartFile file = new MockMultipartFile("file", "profile.jpg", "image/jpeg", new byte[] {1});
        User user = user();
        UserResponseDTO responseDTO = userResponse();
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.of(user));
        when(minioStorageService.uploadImage(file, "profiles/user-1"))
                .thenReturn(new FileUploadResponseDTO("profile.jpg", "image/jpeg", 1L, "profiles/user-1/a.jpg", "internal"));
        when(userRepositoryPort.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(responseDTO);
        when(publicFileUrlService.buildObjectUrl(request, "profiles/1.jpg")).thenReturn("http://public");

        var response = controller.uploadProfileImage("user-1", file, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(minioStorageService).uploadImage(file, "profiles/user-1");
    }

    @Test
    void uploadProfileImage_throwsWhenUserMissing() {
        MockHttpServletRequest request = request("user-1");
        MockMultipartFile file = new MockMultipartFile("file", "profile.jpg", "image/jpeg", new byte[] {1});
        when(userRepositoryPort.findById("user-1")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.uploadProfileImage("user-1", file, request)
        );

        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    @Test
    void ownershipCheck_throwsWhenUserDiffers() {
        MockHttpServletRequest request = request("user-2");

        assertThrows(OwnershipAccessException.class, () -> controller.getById("user-1", request));
    }

    private MockHttpServletRequest request(String userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(authenticationContext.requireCurrentUser(request)).thenReturn(new AuthenticatedUser(userId, "a@a.com"));
        return request;
    }

    private User user() {
        User user = new User();
        user.setId("user-1");
        user.setName("Alvaro");
        user.setEmail("a@a.com");
        user.setProfileImageUrl("profiles/1.jpg");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

    private UserResponseDTO userResponse() {
        return new UserResponseDTO("user-1", "Alvaro", "a@a.com", "profiles/1.jpg", LocalDateTime.now(), LocalDateTime.now());
    }
}
