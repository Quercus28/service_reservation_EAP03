package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RecursoYaAsociadoExceptionTest {

    @Test
    @DisplayName("Indica que el recurso ya estaba asociado al servicio")
    void buildsMessage() {
        RecursoYaAsociadoException ex = new RecursoYaAsociadoException(3, 10);

        assertEquals("El recurso 3 ya está asociado al servicio 10.", ex.getMessage());
        assertInstanceOf(RecursoDominioException.class, ex);
    }
}
