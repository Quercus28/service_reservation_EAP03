package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ServicioNoReservableExceptionTest {

    @Test
    @DisplayName("Indica el id del servicio que no se encontró")
    void buildsMessage() {
        ServicioNoReservableException ex = new ServicioNoReservableException(10);

        assertEquals("No se encontró el servicio con ID: 10", ex.getMessage());
        assertInstanceOf(ReservaDominioException.class, ex);
    }
}
