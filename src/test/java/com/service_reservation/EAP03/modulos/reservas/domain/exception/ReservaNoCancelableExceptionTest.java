package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservaNoCancelableExceptionTest {

    @Test
    @DisplayName("Indica que la reserva ya estaba cancelada")
    void alreadyCancelled() {
        assertEquals("La reserva 8 ya estaba cancelada.", ReservaNoCancelableException.yaCancelada(8).getMessage());
    }

    @Test
    @DisplayName("Indica el estado cuando la reserva ya no está pendiente")
    void notActive() {
        assertEquals("La reserva 8 ya no se puede cancelar porque no está pendiente ni confirmada (su estado es COMPLETADO).",
                ReservaNoCancelableException.noActiva(8, EstadoReserva.COMPLETADO).getMessage());
    }

    @Test
    @DisplayName("Indica que la acción no es válida para el estado actual al cambiar de estado")
    void invalidAction() {
        assertEquals("La acción no es válida para el estado actual de la reserva 8: no se puede pasar de RESERVADO a COMPLETADO.",
                ReservaNoCancelableException.accionNoValida(8, EstadoReserva.RESERVADO, EstadoReserva.COMPLETADO).getMessage());
    }
}
