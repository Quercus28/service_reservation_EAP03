package com.service_reservation.EAP03.modulos.reservas.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReglaDeTurnosTest {

    private static final LocalTime APERTURA = LocalTime.of(8, 0);

    @Test
    @DisplayName("La apertura y cada múltiplo exacto de la duración caen en turno")
    void exactSlotsAreValid() {
        assertTrue(ReglaDeTurnos.caeEnTurno(APERTURA, LocalTime.of(8, 0), 60));
        assertTrue(ReglaDeTurnos.caeEnTurno(APERTURA, LocalTime.of(10, 0), 60));
        assertTrue(ReglaDeTurnos.caeEnTurno(APERTURA, LocalTime.of(8, 45), 45));
    }

    @Test
    @DisplayName("Una hora que no es múltiplo de la duración no cae en turno")
    void misalignedTimesAreInvalid() {
        assertFalse(ReglaDeTurnos.caeEnTurno(APERTURA, LocalTime.of(8, 30), 60));
        assertFalse(ReglaDeTurnos.caeEnTurno(APERTURA, LocalTime.of(9, 0, 30), 60));
        assertFalse(ReglaDeTurnos.caeEnTurno(APERTURA, LocalTime.of(9, 0, 0, 500), 60));
    }

    @Test
    @DisplayName("Una hora anterior a la apertura o una duración no positiva no caen en turno")
    void invalidInputsAreRejected() {
        assertFalse(ReglaDeTurnos.caeEnTurno(APERTURA, LocalTime.of(7, 0), 60));
        assertFalse(ReglaDeTurnos.caeEnTurno(APERTURA, LocalTime.of(9, 0), 0));
        assertFalse(ReglaDeTurnos.caeEnTurno(APERTURA, LocalTime.of(9, 0), -60));
    }
}
