package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ReservaAjenaExceptionTest {

    @Test
    @DisplayName("Indica que la reserva pertenece a otro cliente")
    void buildsMessage() {
        ReservaAjenaException ex = new ReservaAjenaException(8);

        assertEquals("No tiene permiso sobre la reserva 8: pertenece a otro cliente.", ex.getMessage());
        assertInstanceOf(ReservaDominioException.class, ex);
    }
}
