package com.vehiclecare.vehiclecaremicro.domain.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Domain model for an authenticated VehicleCare user.
 *
 * <p>The user aggregate stores identity, login and profile information required by
 * the application services. Timestamp fields capture lifecycle metadata while the
 * password field contains the encoded credential managed by the authentication flow.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {
    private String id;
    private String name;
    private String email;
    private String password;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
