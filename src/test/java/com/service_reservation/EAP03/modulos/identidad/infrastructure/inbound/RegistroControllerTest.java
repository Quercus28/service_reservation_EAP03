package com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.identidad.domain.model.UsuarioNuevoComando;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.RegistrarUsuarioUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RegistroControllerTest {

    @Mock private RegistrarUsuarioUseCase registrarUsuarioUseCase;

    @InjectMocks private RegistroController controller;

    @Test
    @DisplayName("Registro responde 201 y pasa todos los datos al caso de uso")
    void registrationReturnsCreated() {
        // Arrange
        RegistroUsuarioRequestDTO request = new RegistroUsuarioRequestDTO(
                "ana@test.com", "Clave123", "Clave123", "CLIENTE", "Ana", "3001234567", "CC1");

        // Act
        ResponseEntity<Void> response = controller.registrarUsuario(request);

        // Assert
        assertEquals(201, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(registrarUsuarioUseCase).ejecutar(new UsuarioNuevoComando(
                "ana@test.com", "Clave123", "CLIENTE", "Ana", "3001234567", "CC1"));
    }
}
