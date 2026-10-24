package com.service_reservation.EAP03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.Mockito.mockStatic;

@SpringBootTest
class Eap03ApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	@DisplayName("main arranca la aplicación con los argumentos recibidos")
	void mainRunsApplication() {
		// Arrange
		String[] args = {"--server.port=0"};

		try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
			// Act
			Eap03Application.main(args);

			// Assert
			springApplication.verify(() -> SpringApplication.run(Eap03Application.class, args));
		}
	}

}
