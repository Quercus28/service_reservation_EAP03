package com.service_reservation.EAP03.modulos.reservas.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CancelacionFueraDeTiempoExceptionTest {

    @Test
    @DisplayName("Cuando falta poco tiempo indica la anticipación mínima")
    void tooLate() {
        assertEquals("La reserva 8 ya no se puede cancelar: hay que cancelar al menos 2 horas antes de la hora de inicio.",
                CancelacionFueraDeTiempoException.faltaPocoTiempo(8, 2).getMessage());
    }

    @Test
    @DisplayName("Cuando ya empezó indica que ya empezó o ya pasó")
    void alreadyStarted() {
        assertEquals("La reserva 8 no se puede cancelar porque ya empezó o ya pasó.",
                CancelacionFueraDeTiempoException.yaEmpezo(8).getMessage());
    }
}
