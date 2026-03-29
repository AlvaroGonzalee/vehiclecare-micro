package com.vehiclecare.vehiclecaremicro.infrastructure.rest.controller;

import com.vehiclecare.vehiclecaremicro.application.dto.request.AuthLoginRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.request.AuthRegisterRequestDTO;
import com.vehiclecare.vehiclecaremicro.application.dto.response.AuthResponseDTO;
import com.vehiclecare.vehiclecaremicro.application.service.JwtService;
import com.vehiclecare.vehiclecaremicro.domain.model.User;
import com.vehiclecare.vehiclecaremicro.domain.port.in.AuthenticateUserUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.in.CreateUserUseCase;
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

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody AuthRegisterRequestDTO requestDTO) {
        User user = new User();
        user.setName(requestDTO.getName());
        user.setEmail(requestDTO.getEmail());
        user.setPassword(requestDTO.getPassword());
        User created = createUserUseCase.createUser(user);

        AuthResponseDTO response = new AuthResponseDTO(
                created.getId(),
                created.getName(),
                created.getEmail(),
                created.getProfileImageUrl(),
                created.getCreatedAt(),
                jwtService.generateToken(created.getId(), created.getEmail())
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthLoginRequestDTO requestDTO) {
        User user = authenticateUserUseCase.authenticate(requestDTO.getEmail(), requestDTO.getPassword());
        AuthResponseDTO response = new AuthResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProfileImageUrl(),
                user.getCreatedAt(),
                jwtService.generateToken(user.getId(), user.getEmail())
        );
        return ResponseEntity.ok(response);
    }
}
