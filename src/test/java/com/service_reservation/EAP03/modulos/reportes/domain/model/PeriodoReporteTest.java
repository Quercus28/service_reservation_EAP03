package com.service_reservation.EAP03.modulos.reportes.domain.model;

import com.service_reservation.EAP03.modulos.reportes.domain.exception.PeriodoReporteInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PeriodoReporteTest {

    @Test
    @DisplayName("Crea exitosamente un período cuando startDate es anterior a endDate")
    void crearPeriodoValido() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        PeriodoReporte periodo = new PeriodoReporte(start, end);

        assertEquals(start, periodo.startDate());
        assertEquals(end, periodo.endDate());
    }

    @Test
    @DisplayName("Crea exitosamente un período cuando startDate es igual a endDate (período de 1 día)")
    void crearPeriodoMismoDia() {
        LocalDate date = LocalDate.of(2026, 6, 15);

        PeriodoReporte periodo = new PeriodoReporte(date, date);

        assertEquals(date, periodo.startDate());
        assertEquals(date, periodo.endDate());
    }

    @Test
    @DisplayName("Lanza PeriodoReporteInvalidoException cuando endDate es anterior a startDate")
    void rechazarEndDateAnteriorAStartDate() {
        LocalDate start = LocalDate.of(2026, 5, 20);
        LocalDate end = LocalDate.of(2026, 5, 10);

        PeriodoReporteInvalidoException ex = assertThrows(
                PeriodoReporteInvalidoException.class,
                () -> new PeriodoReporte(start, end)
        );

        assertEquals("La fecha de fin no puede ser anterior a la fecha de inicio", ex.getMessage());
    }

    @Test
    @DisplayName("Lanza NullPointerException cuando startDate o endDate son nulos")
    void rechazarFechasNulas() {
        LocalDate date = LocalDate.of(2026, 1, 1);

        assertThrows(NullPointerException.class, () -> new PeriodoReporte(null, date));
        assertThrows(NullPointerException.class, () -> new PeriodoReporte(date, null));
    }
}
