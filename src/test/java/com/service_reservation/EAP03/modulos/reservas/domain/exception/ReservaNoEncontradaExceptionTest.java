package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ReservaNoEncontradaExceptionTest {

    @Test
    @DisplayName("Indica el id de la reserva que no se encontró")
    void buildsMessage() {
        ReservaNoEncontradaException ex = new ReservaNoEncontradaException(8);

        assertEquals("No se encontró la reserva con ID: 8", ex.getMessage());
        assertInstanceOf(ReservaDominioException.class, ex);
    }
}
