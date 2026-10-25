package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DatosInvalidosExceptionTest {

    @Test
    @DisplayName("Conserva el mensaje recibido")
    void buildsMessage() {
        DatosInvalidosException ex = new DatosInvalidosException("Dato inválido");

        assertEquals("Dato inválido", ex.getMessage());
        assertInstanceOf(RecursoDominioException.class, ex);
    }
}
