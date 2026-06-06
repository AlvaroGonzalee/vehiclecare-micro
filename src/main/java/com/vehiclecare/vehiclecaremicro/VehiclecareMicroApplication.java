package com.vehiclecare.vehiclecaremicro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Bootstrap class for the VehicleCare microservice.
 *
 * <p>This class enables Spring Boot auto-configuration, component scanning and the
 * scheduled tasks used by the application. It is the main entry point for local
 * execution, packaged deployments and automated test startup scenarios that rely on
 * the full application context.</p>
 */
@SpringBootApplication
@EnableScheduling
public class VehiclecareMicroApplication {

    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments passed to the JVM process
     */
    public static void main(String[] args) {
        SpringApplication.run(VehiclecareMicroApplication.class, args);
    }
}
