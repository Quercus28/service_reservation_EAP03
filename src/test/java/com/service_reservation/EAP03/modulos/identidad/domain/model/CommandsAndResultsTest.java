package com.service_reservation.EAP03.modulos.identidad.domain.model;

import com.service_reservation.EAP03.modulos.identidad.domain.events.UsuarioRegistradoEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandsAndResultsTest {

    @Test
    @DisplayName("IniciarSesionComando conserva correo y contraseña")
    void loginCommand() {
        // Act
        IniciarSesionComando comando = new IniciarSesionComando("ana@test.com", "Clave123");

        // Assert
        assertEquals("ana@test.com", comando.email());
        assertEquals("Clave123", comando.password());
    }

    @Test
    @DisplayName("UsuarioNuevoComando conserva todos los datos del registro")
    void newUserCommand() {
        // Act
        UsuarioNuevoComando comando = new UsuarioNuevoComando(
                "ana@test.com", "Clave123", "CLIENTE", "Ana", "3001234567", "CC1");

        // Assert
        assertEquals("ana@test.com", comando.email());
        assertEquals("Clave123", comando.password());
        assertEquals("CLIENTE", comando.rol());
        assertEquals("Ana", comando.nombreIdentificacion());
        assertEquals("3001234567", comando.telefono());
        assertEquals("CC1", comando.documentoIdentidad());
    }

    @Test
    @DisplayName("ResultadoConfiguracion2fa conserva la imagen QR y el secreto")
    void twoFactorSetupResult() {
        // Arrange
        byte[] imagen = {1, 2, 3};

        // Act
        ResultadoConfiguracion2fa resultado = new ResultadoConfiguracion2fa(imagen, "SECRETO");

        // Assert
        assertArrayEquals(imagen, resultado.imagenQr());
        assertEquals("SECRETO", resultado.secreto());
    }

    @Test
    @DisplayName("UsuarioRegistradoEvent conserva el id del usuario y su rol")
    void userRegisteredEvent() {
        // Act
        UsuarioRegistradoEvent evento = new UsuarioRegistradoEvent(7L, "ROLE_CLIENTE");

        // Assert
        assertEquals(7L, evento.idUsuario());
        assertEquals("ROLE_CLIENTE", evento.rolNormalizado());
    }
}
