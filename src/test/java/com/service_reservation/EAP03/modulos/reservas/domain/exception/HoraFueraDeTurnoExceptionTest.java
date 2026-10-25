package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HoraFueraDeTurnoExceptionTest {

    @Test
    @DisplayName("Indica la duración de los turnos y la hora de apertura")
    void buildsMessage() {
        assertEquals("Este servicio se reserva cada 60 minutos desde las 08:00.",
                new HoraFueraDeTurnoException(60, LocalTime.of(8, 0)).getMessage());
    }
}
