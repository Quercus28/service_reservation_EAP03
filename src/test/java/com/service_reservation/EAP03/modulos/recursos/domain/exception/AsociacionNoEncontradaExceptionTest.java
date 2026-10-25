package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class AsociacionNoEncontradaExceptionTest {

    @Test
    @DisplayName("Indica qué recurso no está asociado a qué servicio")
    void buildsMessage() {
        AsociacionNoEncontradaException ex = new AsociacionNoEncontradaException(10, 3);

        assertEquals("El recurso 3 no está asociado al servicio 10.", ex.getMessage());
        assertInstanceOf(RecursoDominioException.class, ex);
    }
}
