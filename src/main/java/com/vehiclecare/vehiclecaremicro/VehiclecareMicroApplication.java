package com.vehiclecare.vehiclecaremicro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VehiclecareMicroApplication {

    public static void main(String[] args) {
        SpringApplication.run(VehiclecareMicroApplication.class, args);
    }
}
