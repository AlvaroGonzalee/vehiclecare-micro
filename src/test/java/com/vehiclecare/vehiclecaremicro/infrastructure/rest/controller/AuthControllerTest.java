package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vehiclecare.vehiclecaremicro.application.dto.request.AuthLoginRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.request.AuthRegisterRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.AuthResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.JwtService;
import com.vehiclecare.vehiclecaremicro.application.service.PublicFileUrlService;
import com.vehiclecare.vehiclecaremicro.domain.model.User;
import com.vehiclecare.vehiclecaremicro.domain.port.in.AuthenticateUserUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.CreateUserUseCase;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private CreateUserUseCase createUserUseCase;
    @Mock
    private AuthenticateUserUseCase authenticateUserUseCase;
    @Mock
    private JwtService jwtService;
    @Mock
    private PublicFileUrlService publicFileUrlService;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(createUserUseCase, authenticateUserUseCase, jwtService, publicFileUrlService);
    }

    @Test
    void register_returnsCreatedResponse() {
        AuthRegisterRequestDTO requestDTO = new AuthRegisterRequestDTO("Alvaro", "test@mail.com", "1234");
        User user = user();
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(createUserUseCase.createUser(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(user);
        when(publicFileUrlService.buildObjectUrl(request, "profiles/1.jpg")).thenReturn("http://file");
        when(jwtService.generateToken("user-1", "test@mail.com")).thenReturn("jwt");

        var response = controller.register(requestDTO, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        AuthResponseDTO body = response.getBody();
        assertEquals("user-1", body.getId());
        assertEquals("jwt", body.getToken());
        verify(createUserUseCase).createUser(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void login_returnsOkResponse() {
        AuthLoginRequestDTO requestDTO = new AuthLoginRequestDTO("test@mail.com", "1234");
        User user = user();
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(authenticateUserUseCase.authenticate("test@mail.com", "1234")).thenReturn(user);
        when(publicFileUrlService.buildObjectUrl(request, "profiles/1.jpg")).thenReturn("http://file");
        when(jwtService.generateToken("user-1", "test@mail.com")).thenReturn("jwt");

        var response = controller.login(requestDTO, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt", response.getBody().getToken());
    }

    private User user() {
        User user = new User();
        user.setId("user-1");
        user.setName("Alvaro");
        user.setEmail("test@mail.com");
        user.setProfileImageUrl("profiles/1.jpg");
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
