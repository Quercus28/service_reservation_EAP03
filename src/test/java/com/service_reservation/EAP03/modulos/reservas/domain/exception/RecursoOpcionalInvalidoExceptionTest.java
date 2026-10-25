package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RecursoOpcionalInvalidoExceptionTest {

    @Test
    @DisplayName("Conserva el mensaje del módulo de recursos")
    void buildsMessage() {
        RecursoOpcionalInvalidoException ex = new RecursoOpcionalInvalidoException("No es opcional");

        assertEquals("No es opcional", ex.getMessage());
        assertInstanceOf(ReservaDominioException.class, ex);
    }
}
