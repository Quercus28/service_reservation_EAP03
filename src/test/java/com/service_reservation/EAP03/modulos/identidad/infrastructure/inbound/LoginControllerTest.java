package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.identidad.domain.model.IniciarSesionComando;
import com.service_reservation.EAP03.modulos.identidad.domain.model.TokenRespuesta;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.IniciarSesionUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock private IniciarSesionUseCase iniciarSesionUseCase;

    @InjectMocks private LoginController controller;

    @Test
    @DisplayName("Login exitoso responde 200 con el JWT")
    void successfulLogin() {
        // Arrange
        when(iniciarSesionUseCase.ejecutar(new IniciarSesionComando("ana@test.com", "Clave123")))
                .thenReturn(TokenRespuesta.exitoso("jwt", "Bearer", 3600));

        // Act
        ResponseEntity<LoginResponseDTO> response =
                controller.iniciarSesion(new LoginRequestDTO("ana@test.com", "Clave123"));

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertEquals(new LoginResponseDTO("jwt", "Bearer", 3600), response.getBody());
    }

    @Test
    @DisplayName("Login con 2FA responde 200 con el token temporal")
    void twoFactorLogin() {
        // Arrange
        when(iniciarSesionUseCase.ejecutar(new IniciarSesionComando("admin@test.com", "Clave123")))
                .thenReturn(TokenRespuesta.requiere2fa("temporal"));

        // Act
        ResponseEntity<LoginResponseDTO> response =
                controller.iniciarSesion(new LoginRequestDTO("admin@test.com", "Clave123"));

        // Assert
        assertEquals(new LoginResponseDTO(null, null, 0, true, "temporal"), response.getBody());
    }
}
