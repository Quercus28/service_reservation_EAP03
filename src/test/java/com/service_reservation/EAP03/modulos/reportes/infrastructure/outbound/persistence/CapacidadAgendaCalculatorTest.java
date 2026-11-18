package com.service_reservation.EAP03.modulos.reportes.infrastructure.outbound.persistence;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.reportes.domain.model.PeriodoReporte;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CapacidadAgendaCalculatorTest {

    @Test
    @DisplayName("Calcula correctamente los minutos disponibles para una semana completa de Lunes a Viernes")
    void calcularSemanaLaboral() {
        // Del lunes 23 de noviembre de 2026 al viernes 27 de noviembre de 2026 (5 días hábiles)
        PeriodoReporte periodo = new PeriodoReporte(
                LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 27));

        // Horario: Lunes de 08:00 a 14:00 (6 horas = 360 min)
        Horario hLunes = new Horario(1L, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(14, 0));
        // Horario: Miércoles de 08:00 a 12:00 (4 horas = 240 min)
        Horario hMiercoles = new Horario(2L, DiaSemana.MIERCOLES, LocalTime.of(8, 0), LocalTime.of(12, 0));

        // En esa semana hay 1 Lunes y 1 Miércoles: 360 + 240 = 600 minutos
        long minutos = CapacidadAgendaCalculator.calcularMinutosDisponibles(List.of(hLunes, hMiercoles), periodo);

        assertEquals(600L, minutos);
    }

    @Test
    @DisplayName("Calcula correctamente los minutos cuando el rango abarca varias semanas")
    void calcularVariasSemanas() {
        // 2 semanas: del lunes 2 de noviembre al domingo 15 de noviembre de 2026 (14 días: 2 lunes, 2 martes, etc.)
        PeriodoReporte periodo = new PeriodoReporte(
                LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 15));

        // Horario: Martes de 09:00 a 11:00 (2 horas = 120 min)
        Horario hMartes = new Horario(1L, DiaSemana.MARTES, LocalTime.of(9, 0), LocalTime.of(11, 0));

        // Hay 2 martes en el rango -> 2 * 120 = 240 minutos
        long minutos = CapacidadAgendaCalculator.calcularMinutosDisponibles(List.of(hMartes), periodo);

        assertEquals(240L, minutos);
    }

    @Test
    @DisplayName("Ignora los días que no tienen horarios configurados (ej. fin de semana)")
    void ignorarDiasSinHorario() {
        // Fin de semana: sábado 28 y domingo 29 de noviembre de 2026
        PeriodoReporte periodo = new PeriodoReporte(
                LocalDate.of(2026, 11, 28), LocalDate.of(2026, 11, 29));

        // Horario solo para Lunes
        Horario hLunes = new Horario(1L, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(14, 0));

        long minutos = CapacidadAgendaCalculator.calcularMinutosDisponibles(List.of(hLunes), periodo);

        assertEquals(0L, minutos);
    }

    @Test
    @DisplayName("Retorna 0L cuando la lista de horarios es vacía o nula")
    void horariosVaciosONulos() {
        PeriodoReporte periodo = new PeriodoReporte(
                LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 10));

        assertEquals(0L, CapacidadAgendaCalculator.calcularMinutosDisponibles(Collections.emptyList(), periodo));
        assertEquals(0L, CapacidadAgendaCalculator.calcularMinutosDisponibles(null, periodo));
    }
}
