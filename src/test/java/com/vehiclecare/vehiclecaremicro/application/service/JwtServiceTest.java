package com.vehiclecare.vehiclecaremicro.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.security.JwtAuthenticationException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "super-secret-key";

    private JwtService jwtService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        jwtService = new JwtService(objectMapper, SECRET, 3600);
    }

    @Test
    void generateAndValidateToken_returnsClaims() {
        String token = jwtService.generateToken("user-1", "test@mail.com");

        JwtService.JwtClaims claims = jwtService.validateToken(token);

        assertEquals("user-1", claims.userId());
        assertEquals("test@mail.com", claims.email());
    }

    @Test
    void validateToken_throwsWhenTokenStructureIsInvalid() {
        JwtAuthenticationException ex = assertThrows(
                JwtAuthenticationException.class,
                () -> jwtService.validateToken("bad-token")
        );

        assertEquals("Token JWT inválido", ex.getMessage());
    }

    @Test
    void validateToken_throwsWhenSignatureIsInvalid() {
        String token = jwtService.generateToken("user-1", "test@mail.com");
        String tampered = token.substring(0, token.length() - 1) + "x";

        JwtAuthenticationException ex = assertThrows(
                JwtAuthenticationException.class,
                () -> jwtService.validateToken(tampered)
        );

        assertEquals("Firma JWT inválida", ex.getMessage());
    }

    @Test
    void validateToken_throwsWhenPayloadIsInvalidBase64() {
        String header = encode(Map.of("alg", "HS256", "typ", "JWT"));
        String payload = "%%%";
        String signature = sign(header + "." + payload);

        JwtAuthenticationException ex = assertThrows(
                JwtAuthenticationException.class,
                () -> jwtService.validateToken(header + "." + payload + "." + signature)
        );

        assertEquals("Payload JWT inválido", ex.getMessage());
    }

    @Test
    void validateToken_throwsWhenTokenExpired() {
        JwtService expiredService = new JwtService(objectMapper, SECRET, -1);
        String token = expiredService.generateToken("user-1", "test@mail.com");

        JwtAuthenticationException ex = assertThrows(
                JwtAuthenticationException.class,
                () -> expiredService.validateToken(token)
        );

        assertEquals("Token JWT expirado", ex.getMessage());
    }

    @Test
    void validateToken_throwsWhenSubClaimMissing() {
        String token = buildToken(Map.of(
                "email", "test@mail.com",
                "iat", 1,
                "exp", 9999999999L
        ));

        JwtAuthenticationException ex = assertThrows(
                JwtAuthenticationException.class,
                () -> jwtService.validateToken(token)
        );

        assertEquals("Falta el claim JWT: sub", ex.getMessage());
    }

    @Test
    void validateToken_throwsWhenEmailClaimMissing() {
        String token = buildToken(Map.of(
                "sub", "user-1",
                "iat", 1,
                "exp", 9999999999L
        ));

        JwtAuthenticationException ex = assertThrows(
                JwtAuthenticationException.class,
                () -> jwtService.validateToken(token)
        );

        assertEquals("Falta el claim JWT: email", ex.getMessage());
    }

    @Test
    void validateToken_throwsWhenExpirationClaimMissing() {
        String token = buildToken(Map.of(
                "sub", "user-1",
                "email", "test@mail.com",
                "iat", 1
        ));

        JwtAuthenticationException ex = assertThrows(
                JwtAuthenticationException.class,
                () -> jwtService.validateToken(token)
        );

        assertEquals("Falta el claim JWT: exp", ex.getMessage());
    }

    @Test
    void validateToken_throwsWhenExpirationClaimInvalid() {
        String token = buildToken(Map.of(
                "sub", "user-1",
                "email", "test@mail.com",
                "iat", 1,
                "exp", "bad"
        ));

        JwtAuthenticationException ex = assertThrows(
                JwtAuthenticationException.class,
                () -> jwtService.validateToken(token)
        );

        assertEquals("Claim JWT inválido: exp", ex.getMessage());
    }

    @Test
    void validateToken_acceptsExpirationClaimAsNumericString() {
        String token = buildToken(Map.of(
                "sub", "user-1",
                "email", "test@mail.com",
                "iat", 1,
                "exp", "9999999999"
        ));

        JwtService.JwtClaims claims = jwtService.validateToken(token);

        assertEquals("user-1", claims.userId());
        assertEquals("test@mail.com", claims.email());
        assertEquals(9999999999L, claims.expiresAt());
    }

    @Test
    void generateToken_throwsWhenPayloadCannotBeSerialized() throws Exception {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsBytes(anyMap())).thenThrow(new RuntimeException("boom"));
        JwtService failingService = new JwtService(failingMapper, SECRET, 3600);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> failingService.generateToken("user-1", "test@mail.com")
        );

        assertEquals("No se pudo serializar el JWT", ex.getMessage());
    }

    @Test
    void validateToken_throwsWhenTokenCannotBeSigned() throws Exception {
        Field signingKeyField = JwtService.class.getDeclaredField("signingKey");
        signingKeyField.setAccessible(true);
        signingKeyField.set(jwtService, null);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> jwtService.validateToken("a.b.c")
        );

        assertEquals("No se pudo firmar el JWT", ex.getMessage());
    }

    private String buildToken(Map<String, Object> payload) {
        String header = encode(Map.of("alg", "HS256", "typ", "JWT"));
        String encodedPayload = encode(payload);
        String content = header + "." + encodedPayload;
        return content + "." + sign(content);
    }

    private String encode(Map<String, Object> value) {
        try {
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
