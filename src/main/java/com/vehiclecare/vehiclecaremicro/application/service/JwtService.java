package com.vehiclecare.vehiclecaremicro.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehiclecare.vehiclecaremicro.infrastructure.security.JwtAuthenticationException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Provides JWT creation and validation without relying on an external security library.
 *
 * <p>The service generates HS256-signed tokens containing the authenticated user's
 * identifier and email, and validates incoming tokens by checking structure, signature
 * and expiration claims. It acts as the core token component used by the authentication
 * endpoints and the request interceptor.</p>
 */
@Service
@Slf4j
public class JwtService {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() { };

    private final ObjectMapper objectMapper;
    private final byte[] signingKey;
    private final long expirationSeconds;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-seconds:86400}") long expirationSeconds
    ) {
        this.objectMapper = objectMapper;
        this.signingKey = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    /**
     * Generates a signed JWT for the given user.
     *
     * @param userId unique user identifier stored as the subject claim
     * @param email normalized user email stored as a custom claim
     * @return compact JWT string
     */
    public String generateToken(String userId, String email) {
        log.debug("Generating JWT token userId={} email={}", userId, email);
        Instant now = Instant.now();
        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", userId);
        payload.put("email", email);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plusSeconds(expirationSeconds).getEpochSecond());

        String encodedHeader = encodeJson(header);
        String encodedPayload = encodeJson(payload);
        String content = encodedHeader + "." + encodedPayload;
        return content + "." + sign(content);
    }

    /**
     * Validates a JWT and extracts the claims required by the application.
     *
     * @param token compact JWT string received from the client
     * @return validated claim set
     * @throws JwtAuthenticationException if the token is malformed, invalid or expired
     */
    public JwtClaims validateToken(String token) {
        log.debug("Validating JWT token");
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new JwtAuthenticationException("Token JWT inválido");
        }

        String content = parts[0] + "." + parts[1];
        String expectedSignature = sign(content);
        if (!expectedSignature.equals(parts[2])) {
            throw new JwtAuthenticationException("Firma JWT inválida");
        }

        Map<String, Object> payload = decodeJson(parts[1]);
        String subject = stringClaim(payload, "sub");
        String email = stringClaim(payload, "email");
        long expiration = longClaim(payload, "exp");
        if (Instant.now().getEpochSecond() >= expiration) {
            throw new JwtAuthenticationException("Token JWT expirado");
        }

        log.debug("JWT token validated userId={} email={} expiresAt={}", subject, email, expiration);
        return new JwtClaims(subject, email, expiration);
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo serializar el JWT", ex);
        }
    }

    private Map<String, Object> decodeJson(String value) {
        try {
            byte[] decoded = URL_DECODER.decode(value);
            return objectMapper.readValue(decoded, MAP_TYPE);
        } catch (Exception ex) {
            throw new JwtAuthenticationException("Payload JWT inválido", ex);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(signingKey, HMAC_SHA256));
            return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo firmar el JWT", ex);
        }
    }

    private String stringClaim(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null) {
            throw new JwtAuthenticationException("Falta el claim JWT: " + key);
        }
        return value.toString();
    }

    private long longClaim(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            throw new JwtAuthenticationException("Falta el claim JWT: " + key);
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ex) {
            throw new JwtAuthenticationException("Claim JWT inválido: " + key, ex);
        }
    }

    /**
     * Immutable view of the JWT claims consumed by the application layer.
     *
     * @param userId authenticated user identifier
     * @param email authenticated user email
     * @param expiresAt expiration instant as epoch seconds
     */
    public record JwtClaims(String userId, String email, long expiresAt) {
    }
}
