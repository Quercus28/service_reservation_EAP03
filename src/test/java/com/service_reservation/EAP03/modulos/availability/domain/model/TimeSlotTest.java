package com.service_reservation.EAP03.modulos.availability.domain.model;

import com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeSlotTest {

    private static final LocalDateTime OCHO = LocalDateTime.of(2026, 11, 2, 8, 0);

    private TimeSlot slot(int horaInicio, int minInicio, int horaFin, int minFin) {
        return new TimeSlot(OCHO.withHour(horaInicio).withMinute(minInicio), OCHO.withHour(horaFin).withMinute(minFin));
    }

    @Test
    @DisplayName("Crea la franja cuando el fin es posterior al inicio")
    void creaFranjaValida() {
        TimeSlot slot = slot(8, 0, 9, 0);

        assertEquals(OCHO, slot.start());
        assertEquals(OCHO.plusHours(1), slot.end());
    }

    @Test
    @DisplayName("Rechaza inicio o fin nulos")
    void rechazaNulos() {
        assertThrows(NullPointerException.class, () -> new TimeSlot(null, OCHO));
        assertThrows(NullPointerException.class, () -> new TimeSlot(OCHO, null));
    }

    @Test
    @DisplayName("Rechaza una franja con fin igual o anterior al inicio")
    void rechazaFinNoPosterior() {
        assertThrows(DisponibilidadInvalidaException.class, () -> new TimeSlot(OCHO, OCHO));
        assertThrows(DisponibilidadInvalidaException.class, () -> new TimeSlot(OCHO, OCHO.minusMinutes(1)));
    }

    @Test
    @DisplayName("Detecta solapamiento parcial y total")
    void detectaSolapamiento() {
        TimeSlot base = slot(8, 0, 9, 0);

        assertTrue(base.seSolapaCon(slot(8, 30, 9, 30)));
        assertTrue(base.seSolapaCon(slot(7, 30, 8, 30)));
        assertTrue(base.seSolapaCon(slot(8, 15, 8, 45)));
        assertTrue(base.seSolapaCon(slot(7, 0, 10, 0)));
    }

    @Test
    @DisplayName("Franjas contiguas o separadas no se solapan, ni con null")
    void noSolapaContiguasNiNull() {
        TimeSlot base = slot(8, 0, 9, 0);

        assertFalse(base.seSolapaCon(slot(9, 0, 10, 0)));
        assertFalse(base.seSolapaCon(slot(7, 0, 8, 0)));
        assertFalse(base.seSolapaCon(slot(11, 0, 12, 0)));
        assertFalse(base.seSolapaCon(null));
    }

    @Test
    @DisplayName("Contiene franjas internas o iguales, pero no las que sobresalen")
    void contiene() {
        TimeSlot base = slot(8, 0, 10, 0);

        assertTrue(base.contiene(slot(8, 0, 10, 0)));
        assertTrue(base.contiene(slot(8, 30, 9, 30)));
        assertFalse(base.contiene(slot(7, 30, 9, 0)));
        assertFalse(base.contiene(slot(9, 0, 10, 30)));
        assertFalse(base.contiene(null));
    }

    @Test
    @DisplayName("Ordena por inicio y, ante empate, por fin")
    void comparaPorInicioYFin() {
        TimeSlot temprano = slot(8, 0, 9, 0);
        TimeSlot tarde = slot(9, 0, 10, 0);
        TimeSlot tempranoLargo = slot(8, 0, 10, 0);

        assertTrue(temprano.compareTo(tarde) < 0);
        assertTrue(tarde.compareTo(temprano) > 0);
        assertTrue(temprano.compareTo(tempranoLargo) < 0);
        assertEquals(0, temprano.compareTo(slot(8, 0, 9, 0)));
    }
}
