package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RecursoNoDisponibleExceptionTest {

    @Test
    @DisplayName("Conserva el mensaje del módulo de recursos")
    void buildsMessage() {
        RecursoNoDisponibleException ex = new RecursoNoDisponibleException("Sin unidades");

        assertEquals("Sin unidades", ex.getMessage());
        assertInstanceOf(ReservaDominioException.class, ex);
    }
}
