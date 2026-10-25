package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DatosReservaInvalidosExceptionTest {

    @Test
    @DisplayName("Conserva el mensaje recibido")
    void buildsMessage() {
        DatosReservaInvalidosException ex = new DatosReservaInvalidosException("Dato inválido");

        assertEquals("Dato inválido", ex.getMessage());
        assertInstanceOf(ReservaDominioException.class, ex);
    }
}
