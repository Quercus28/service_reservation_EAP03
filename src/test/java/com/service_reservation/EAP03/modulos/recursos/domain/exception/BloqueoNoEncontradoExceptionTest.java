package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class BloqueoNoEncontradoExceptionTest {

    @Test
    @DisplayName("Indica el bloqueo y el recurso que no se encontraron")
    void buildsMessage() {
        BloqueoNoEncontradoException ex = new BloqueoNoEncontradoException(1, 8);

        assertEquals("No se encontró el bloqueo con ID 8 para el recurso 1", ex.getMessage());
        assertInstanceOf(RecursoDominioException.class, ex);
    }
}
