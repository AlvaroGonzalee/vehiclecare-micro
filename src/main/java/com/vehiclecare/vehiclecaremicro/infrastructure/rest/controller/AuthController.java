package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.dto.request.AuthLoginRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.request.AuthRegisterRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.AuthResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.JwtService;
import com.vehiclecare.vehiclecaremicro.application.service.PublicFileUrlService;
import com.vehiclecare.vehiclecaremicro.domain.model.User;
import com.vehiclecare.vehiclecaremicro.domain.port.in.AuthenticateUserUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.CreateUserUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CreateUserUseCase createUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final JwtService jwtService;
    private final PublicFileUrlService publicFileUrlService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody AuthRegisterRequestDTO requestDTO,
            HttpServletRequest request
    ) {
        User user = new User();
        user.setName(requestDTO.getName());
        user.setEmail(requestDTO.getEmail());
        user.setPassword(requestDTO.getPassword());
        User created = createUserUseCase.createUser(user);

        AuthResponseDTO response = toAuthResponse(created, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody AuthLoginRequestDTO requestDTO,
            HttpServletRequest request
    ) {
        User user = authenticateUserUseCase.authenticate(requestDTO.getEmail(), requestDTO.getPassword());
        AuthResponseDTO response = toAuthResponse(user, request);
        return ResponseEntity.ok(response);
    }

    private AuthResponseDTO toAuthResponse(User user, HttpServletRequest request) {
        return new AuthResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                publicFileUrlService.buildObjectUrl(request, user.getProfileImageUrl()),
                user.getCreatedAt(),
                jwtService.generateToken(user.getId(), user.getEmail())
        );
    }
}
