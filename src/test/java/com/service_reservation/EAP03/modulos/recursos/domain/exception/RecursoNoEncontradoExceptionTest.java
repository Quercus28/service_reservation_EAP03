package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RecursoNoEncontradoExceptionTest {

    @Test
    @DisplayName("Indica el id del recurso que no se encontró")
    void buildsMessage() {
        RecursoNoEncontradoException ex = new RecursoNoEncontradoException(9);

        assertEquals("No se encontró el recurso con ID: 9", ex.getMessage());
        assertInstanceOf(RecursoDominioException.class, ex);
    }
}
