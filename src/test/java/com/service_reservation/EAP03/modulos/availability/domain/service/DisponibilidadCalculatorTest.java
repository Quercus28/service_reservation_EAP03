package com.service_reservation.EAP03.modulos.availability.domain.service;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException;
import com.service_reservation.EAP03.modulos.availability.domain.model.DateRange;
import com.service_reservation.EAP03.modulos.availability.domain.model.HorarioSemanal;
import com.service_reservation.EAP03.modulos.availability.domain.model.TimeSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DisponibilidadCalculatorTest {

    // 2026-11-02 es lunes
    private static final LocalDate LUNES = LocalDate.of(2026, 11, 2);
    private static final DateRange SOLO_LUNES = new DateRange(LUNES, LUNES);

    private final DisponibilidadCalculator calculator = new DisponibilidadCalculator();

    private HorarioSemanal horario(DiaSemana dia, int desde, int hasta) {
        return new HorarioSemanal(dia, LocalTime.of(desde, 0), LocalTime.of(hasta, 0));
    }

    private TimeSlot franja(LocalDate fecha, int hIni, int mIni, int hFin, int mFin) {
        return new TimeSlot(fecha.atTime(hIni, mIni), fecha.atTime(hFin, mFin));
    }

    @Test
    @DisplayName("Rechaza un rango de fechas nulo")
    void rechazaRangoNulo() {
        assertThrows(NullPointerException.class,
                () -> calculator.calcularDisponibilidad(null, List.of(), List.of(), 60));
    }

    @Test
    @DisplayName("Rechaza una duración de turno cero o negativa")
    void rechazaDuracionInvalida() {
        List<HorarioSemanal> horarios = List.of(horario(DiaSemana.LUNES, 8, 10));

        assertThrows(DisponibilidadInvalidaException.class,
                () -> calculator.calcularDisponibilidad(SOLO_LUNES, horarios, List.of(), 0));
        assertThrows(DisponibilidadInvalidaException.class,
                () -> calculator.calcularDisponibilidad(SOLO_LUNES, horarios, List.of(), -30));
    }

    @Test
    @DisplayName("Sin horarios configurados no hay disponibilidad")
    void sinHorariosDevuelveVacio() {
        assertTrue(calculator.calcularDisponibilidad(SOLO_LUNES, null, List.of(), 60).isEmpty());
        assertTrue(calculator.calcularDisponibilidad(SOLO_LUNES, List.of(), List.of(), 60).isEmpty());
    }

    @Test
    @DisplayName("Divide la ventana de atención en turnos de la duración del servicio")
    void segmentaVentanaEnTurnos() {
        List<TimeSlot> slots = calculator.calcularDisponibilidad(
                SOLO_LUNES, List.of(horario(DiaSemana.LUNES, 8, 10)), List.of(), 60);

        assertEquals(List.of(franja(LUNES, 8, 0, 9, 0), franja(LUNES, 9, 0, 10, 0)), slots);
    }

    @Test
    @DisplayName("Descarta el último turno si no cabe completo en la ventana")
    void descartaTurnoIncompleto() {
        HorarioSemanal ventana = new HorarioSemanal(DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(9, 30));

        List<TimeSlot> slots = calculator.calcularDisponibilidad(SOLO_LUNES, List.of(ventana), List.of(), 60);

        assertEquals(List.of(franja(LUNES, 8, 0, 9, 0)), slots);
    }

    @Test
    @DisplayName("Ventana más corta que la duración no genera turnos")
    void ventanaMasCortaQueDuracion() {
        List<TimeSlot> slots = calculator.calcularDisponibilidad(
                SOLO_LUNES, List.of(horario(DiaSemana.LUNES, 8, 9)), List.of(), 90);

        assertTrue(slots.isEmpty());
    }

    @Test
    @DisplayName("Excluye los turnos que se solapan con reservas ocupadas")
    void excluyeTurnosOcupados() {
        List<TimeSlot> ocupadas = List.of(franja(LUNES, 8, 30, 9, 0));

        List<TimeSlot> slots = calculator.calcularDisponibilidad(
                SOLO_LUNES, List.of(horario(DiaSemana.LUNES, 8, 11)), ocupadas, 60);

        assertEquals(List.of(franja(LUNES, 9, 0, 10, 0), franja(LUNES, 10, 0, 11, 0)), slots);
    }

    @Test
    @DisplayName("Una reserva contigua a un turno no lo bloquea")
    void reservaContiguaNoBloquea() {
        List<TimeSlot> ocupadas = List.of(franja(LUNES, 7, 0, 8, 0));

        List<TimeSlot> slots = calculator.calcularDisponibilidad(
                SOLO_LUNES, List.of(horario(DiaSemana.LUNES, 8, 9)), ocupadas, 60);

        assertEquals(List.of(franja(LUNES, 8, 0, 9, 0)), slots);
    }

    @Test
    @DisplayName("Acepta lista de reservas nula como si no hubiera ocupación")
    void reservasNulas() {
        List<TimeSlot> slots = calculator.calcularDisponibilidad(
                SOLO_LUNES, List.of(horario(DiaSemana.LUNES, 8, 10)), null, 60);

        assertEquals(2, slots.size());
    }

    @Test
    @DisplayName("No genera turnos para días sin horario configurado")
    void diaSinHorario() {
        List<TimeSlot> slots = calculator.calcularDisponibilidad(
                SOLO_LUNES, List.of(horario(DiaSemana.MARTES, 8, 10)), List.of(), 60);

        assertTrue(slots.isEmpty());
    }

    @Test
    @DisplayName("Mapea correctamente cada día de la semana en un rango de 7 días")
    void mapeaTodosLosDiasDeLaSemana() {
        List<HorarioSemanal> horarios = Arrays.stream(DiaSemana.values())
                .map(dia -> horario(dia, 8 + dia.ordinal(), 9 + dia.ordinal()))
                .toList();

        List<TimeSlot> slots = calculator.calcularDisponibilidad(
                new DateRange(LUNES, LUNES.plusDays(6)), horarios, List.of(), 60);

        assertEquals(7, slots.size());
        for (int i = 0; i < 7; i++) {
            LocalDate fecha = LUNES.plusDays(i);
            assertEquals(franja(fecha, 8 + i, 0, 9 + i, 0), slots.get(i),
                    "Turno incorrecto para " + fecha.getDayOfWeek());
        }
    }

    @Test
    @DisplayName("Combina varias ventanas del mismo día y devuelve los turnos ordenados")
    void ordenaTurnosDeVariasVentanas() {
        List<HorarioSemanal> horarios = List.of(
                horario(DiaSemana.LUNES, 14, 15),
                horario(DiaSemana.LUNES, 8, 9));

        List<TimeSlot> slots = calculator.calcularDisponibilidad(SOLO_LUNES, horarios, List.of(), 60);

        assertEquals(List.of(franja(LUNES, 8, 0, 9, 0), franja(LUNES, 14, 0, 15, 0)), slots);
    }

    @Test
    @DisplayName("Repite el horario semanal en cada semana del rango")
    void repiteHorarioCadaSemana() {
        List<TimeSlot> slots = calculator.calcularDisponibilidad(
                new DateRange(LUNES, LUNES.plusDays(13)), List.of(horario(DiaSemana.LUNES, 8, 9)), List.of(), 60);

        assertEquals(List.of(
                franja(LUNES, 8, 0, 9, 0),
                franja(LUNES.plusDays(7), 8, 0, 9, 0)), slots);
    }

    @Test
    @DisplayName("Una reserva de otro día no afecta los turnos del día consultado")
    void reservaDeOtroDiaNoAfecta() {
        LocalDateTime martes = LUNES.plusDays(1).atTime(8, 0);
        List<TimeSlot> ocupadas = List.of(new TimeSlot(martes, martes.plusHours(1)));

        List<TimeSlot> slots = calculator.calcularDisponibilidad(
                SOLO_LUNES, List.of(horario(DiaSemana.LUNES, 8, 9)), ocupadas, 60);

        assertEquals(1, slots.size());
    }
}
