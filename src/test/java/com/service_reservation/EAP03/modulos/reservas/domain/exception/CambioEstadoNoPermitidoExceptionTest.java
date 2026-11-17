package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CambioEstadoNoPermitidoExceptionTest {

    @Test
    @DisplayName("Indica que la acción no es válida para el estado actual y qué cambio se pidió")
    void buildsMessage() {
        CambioEstadoNoPermitidoException ex =
                new CambioEstadoNoPermitidoException(8, EstadoReserva.RESERVADO, EstadoReserva.COMPLETADO);

        assertEquals("La acción no es válida para el estado actual de la reserva 8: no se puede pasar de RESERVADO a COMPLETADO.",
                ex.getMessage());
        assertInstanceOf(ReservaDominioException.class, ex);
    }
}
