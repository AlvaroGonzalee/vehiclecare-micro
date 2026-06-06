package com.vehiclecare.vehiclecaremicro.application.usecase;

import com.vehiclecare.vehiclecaremicro.domain.model.User;
import com.vehiclecare.vehiclecaremicro.domain.port.in.AuthenticateUserUseCase;
import com.vehiclecare.vehiclecaremicro.domain.port.out.UserRepositoryPort;
import com.vehiclecare.vehiclecaremicro.infrastructure.rest.exception.AuthenticationFailedException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authenticates a user by email and password.
 *
 * <p>This use case normalizes the incoming email address, retrieves the user through
 * the repository port and verifies the raw password against the stored encoded value.
 * On success it returns the authenticated domain user; otherwise it raises a dedicated
 * authentication exception.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticateUserUseCaseImpl implements AuthenticateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    /**
     * Authenticates a user with the supplied credentials.
     *
     * @param email raw email received from the client
     * @param password raw password received from the client
     * @return authenticated user
     */
    @Override
    public User authenticate(String email, String password) {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
        log.info("Authenticating user email={}", normalizedEmail);
        User user = userRepositoryPort.findByEmail(normalizedEmail)
                .orElseThrow(() -> new AuthenticationFailedException("Credenciales inválidas"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Authentication failed due to password mismatch email={}", normalizedEmail);
            throw new AuthenticationFailedException("Credenciales inválidas");
        }
        log.info("User authenticated successfully userId={} email={}", user.getId(), user.getEmail());
        return user;
    }
}
