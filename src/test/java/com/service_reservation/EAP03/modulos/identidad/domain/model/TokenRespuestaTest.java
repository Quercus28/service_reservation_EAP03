package com.service_reservation.EAP03.modulos.identidad.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenRespuestaTest {

    @Test
    @DisplayName("Una respuesta exitosa lleva el token y no requiere 2FA")
    void successfulResponse() {
        // Act
        TokenRespuesta respuesta = TokenRespuesta.exitoso("jwt", "Bearer", 3600);

        // Assert
        assertEquals("jwt", respuesta.token());
        assertEquals("Bearer", respuesta.tipo());
        assertEquals(3600, respuesta.expiracionSegundos());
        assertFalse(respuesta.requiere2fa());
        assertNull(respuesta.tokenTemporal());
    }

    @Test
    @DisplayName("Una respuesta que requiere 2FA solo lleva el token temporal")
    void twoFactorRequiredResponse() {
        // Act
        TokenRespuesta respuesta = TokenRespuesta.requiere2fa("temporal");

        // Assert
        assertNull(respuesta.token());
        assertNull(respuesta.tipo());
        assertEquals(0, respuesta.expiracionSegundos());
        assertTrue(respuesta.requiere2fa());
        assertEquals("temporal", respuesta.tokenTemporal());
    }

    @Test
    @DisplayName("El constructor de tres campos equivale a una respuesta exitosa")
    void threeFieldConstructor() {
        // Act
        TokenRespuesta respuesta = new TokenRespuesta("jwt", "Bearer", 60);

        // Assert
        assertEquals(TokenRespuesta.exitoso("jwt", "Bearer", 60), respuesta);
    }
}
