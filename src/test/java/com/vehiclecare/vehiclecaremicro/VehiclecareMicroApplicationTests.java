package com.vehiclecare.vehiclecaremicro;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringApplication;

@SpringBootTest
class VehiclecareMicroApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void main_startsApplication() {
		try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
			String[] args = new String[] {"--test"};
			VehiclecareMicroApplication.main(args);

			springApplication.verify(() -> SpringApplication.run(eq(VehiclecareMicroApplication.class), same(args)));
		}
	}

}
