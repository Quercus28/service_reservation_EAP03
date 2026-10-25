package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FechaEnElPasadoExceptionTest {

    @Test
    @DisplayName("Indica la fecha de inicio que ya pasó")
    void buildsMessage() {
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 1, 9, 0);

        assertEquals("No se puede reservar en el pasado: la fecha de inicio " + fecha + " ya pasó.",
                new FechaEnElPasadoException(fecha).getMessage());
    }
}
