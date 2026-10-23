package com.service_reservation.EAP03.modulos.availability.domain.service;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException;
import com.service_reservation.EAP03.modulos.availability.domain.model.DateRange;
import com.service_reservation.EAP03.modulos.availability.domain.model.HorarioSemanal;
import com.service_reservation.EAP03.modulos.availability.domain.model.TimeSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

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
        // Arrange
        List<HorarioSemanal> horarios = List.of(horario(DiaSemana.LUNES, 8, 10));

        // Act
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> calculator.calcularDisponibilidad(null, horarios, List.of(), 60));

        // Assert
        assertEquals("El rango de fechas no puede ser nulo", ex.getMessage());
    }

    @ParameterizedTest(name = "duración = {0}")
    @ValueSource(ints = {0, -30})
    @DisplayName("Rechaza una duración de turno cero o negativa")
    void rechazaDuracionInvalida(int duracion) {
        // Arrange
        List<HorarioSemanal> horarios = List.of(horario(DiaSemana.LUNES, 8, 10));

        // Act
        DisponibilidadInvalidaException ex = assertThrows(DisponibilidadInvalidaException.class,
                () -> calculator.calcularDisponibilidad(SOLO_LUNES, horarios, List.of(), duracion));

        // Assert
        assertEquals("La duración del turno debe ser mayor a 0 minutos", ex.getMessage());
    }

    @Test
    @DisplayName("Sin horarios configurados (null) no hay disponibilidad")
    void horariosNulosDevuelveVacio() {
        // Arrange
        List<HorarioSemanal> horarios = null;

        // Act
        List<TimeSlot> slots = calculator.calcularDisponibilidad(SOLO_LUNES, horarios, List.of(), 60);

        // Assert
        assertTrue(slots.isEmpty());
    }

    @Test
    @DisplayName("Sin horarios configurados (lista vacía) no hay disponibilidad")
    void horariosVaciosDevuelveVacio() {
        // Arrange
        List<HorarioSemanal> horarios = List.of();

        // Act
        List<TimeSlot> slots = calculator.calcularDisponibilidad(SOLO_LUNES, horarios, List.of(), 60);

        // Assert
        assertTrue(slots.isEmpty());
    }

    @Test
    @DisplayName("Divide la ventana de atención en turnos de la duración del servicio")
    void segmentaVentanaEnTurnos() {
        // Arrange
        List<HorarioSemanal> horarios = List.of(horario(DiaSemana.LUNES, 8, 10));

        // Act
        List<TimeSlot> slots = calculator.calcularDisponibilidad(SOLO_LUNES, horarios, List.of(), 60);

        // Assert
        assertEquals(List.of(franja(LUNES, 8, 0, 9, 0), franja(LUNES, 9, 0, 10, 0)), slots);
    }

    @Test
    @DisplayName("Descarta el último turno si no cabe completo en la ventana")
    void descartaTurnoIncompleto() {
        // Arrange
        List<HorarioSemanal> horarios = List.of(
                new HorarioSemanal(DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(9, 30)));

        // Act
        List<TimeSlot> slots = calculator.calcularDisponibilidad(SOLO_LUNES, horarios, List.of(), 60);

        // Assert
        assertEquals(List.of(franja(LUNES, 8, 0, 9, 0)), slots);
    }

    @Test
    @DisplayName("Ventana más corta que la duración no genera turnos")
    void ventanaMasCortaQueDuracion() {
        // Arrange
        List<HorarioSemanal> horarios = List.of(horario(DiaSemana.LUNES, 8, 9));

        // Act
        List<TimeSlot> slots = calculator.calcularDisponibilidad(SOLO_LUNES, horarios, List.of(), 90);

        // Assert
        assertTrue(slots.isEmpty());
    }

    @Test
    @DisplayName("Excluye los turnos que se solapan con reservas ocupadas")
    void excluyeTurnosOcupados() {
        // Arrange
        List<HorarioSemanal> horarios = List.of(horario(DiaSemana.LUNES, 8, 11));
        List<TimeSlot> ocupadas = List.of(franja(LUNES, 8, 30, 9, 0));

        // Act
        List<TimeSlot> slots = calculator.calcularDisponibilidad(SOLO_LUNES, horarios, ocupadas, 60);

        // Assert
        assertEquals(List.of(franja(LUNES, 9, 0, 10, 0), franja(LUNES, 10, 0, 11, 0)), slots);
    }

    @Test
    @DisplayName("Una reserva contigua a un turno no lo bloquea")
    void reservaContiguaNoBloquea() {
        // Arrange
        List<HorarioSemanal> horarios = List.of(horario(DiaSemana.LUNES, 8, 9));
        List<TimeSlot> ocupadas = List.of(franja(LUNES, 7, 0, 8, 0));

        // Act
        List<TimeSlot> slots = calculator.calcularDisponibilidad(SOLO_LUNES, horarios, ocupadas, 60);

        // Assert
        assertEquals(List.of(franja(LUNES, 8, 0, 9, 0)), slots);
    }

    @Test
    @DisplayName("Acepta lista de reservas nula como si no hubiera ocupación")
    void reservasNulas() {
        // Arrange
        List<HorarioSemanal> horarios = List.of(horario(DiaSemana.LUNES, 8, 10));
        List<TimeSlot> ocupadas = null;

        // Act
        List<TimeSlot> slots = calculator.calcularDisponibilidad(SOLO_LUNES, horarios, ocupadas, 60);

        // Assert
        assertEquals(2, slots.size());
    }

    @Test
    @DisplayName("No genera turnos para días sin horario configurado")
    void diaSinHorario() {
        // Arrange
        List<HorarioSemanal> horarios = List.of(horario(DiaSemana.MARTES, 8, 10));

        // Act
        List<TimeSlot> slots = calculator.calcularDisponibilidad(SOLO_LUNES, horarios, List.of(), 60);

        // Assert
        assertTrue(slots.isEmpty());
    }

    @Test
    @DisplayName("Mapea correctamente cada día de la semana en un rango de 7 días")
    void mapeaTodosLosDiasDeLaSemana() {
        // Arrange: cada día tiene una única hora de atención distinta (lunes 8-9, martes 9-10, ...)
        List<HorarioSemanal> horarios = Arrays.stream(DiaSemana.values())
                .map(dia -> horario(dia, 8 + dia.ordinal(), 9 + dia.ordinal()))
                .toList();
        DateRange semana = new DateRange(LUNES, LUNES.plusDays(6));
        List<TimeSlot> esperado = IntStream.range(0, 7)
                .mapToObj(i -> franja(LUNES.plusDays(i), 8 + i, 0, 9 + i, 0))
                .toList();

        // Act
        List<TimeSlot> slots = calculator.calcularDisponibilidad(semana, horarios, List.of(), 60);

        // Assert
        assertEquals(esperado, slots);
    }

    @Test
    @DisplayName("Combina varias ventanas del mismo día y devuelve los turnos ordenados")
    void ordenaTurnosDeVariasVentanas() {
        // Arrange
        List<HorarioSemanal> horarios = List.of(
                horario(DiaSemana.LUNES, 14, 15),
                horario(DiaSemana.LUNES, 8, 9));

        // Act
        List<TimeSlot> slots = calculator.calcularDisponibilidad(SOLO_LUNES, horarios, List.of(), 60);

        // Assert
        assertEquals(List.of(franja(LUNES, 8, 0, 9, 0), franja(LUNES, 14, 0, 15, 0)), slots);
    }

    @Test
    @DisplayName("Repite el horario semanal en cada semana del rango")
    void repiteHorarioCadaSemana() {
        // Arrange
        List<HorarioSemanal> horarios = List.of(horario(DiaSemana.LUNES, 8, 9));
        DateRange dosSemanas = new DateRange(LUNES, LUNES.plusDays(13));

        // Act
        List<TimeSlot> slots = calculator.calcularDisponibilidad(dosSemanas, horarios, List.of(), 60);

        // Assert
        assertEquals(List.of(
                franja(LUNES, 8, 0, 9, 0),
                franja(LUNES.plusDays(7), 8, 0, 9, 0)), slots);
    }

    @Test
    @DisplayName("Una reserva de otro día no afecta los turnos del día consultado")
    void reservaDeOtroDiaNoAfecta() {
        // Arrange
        List<HorarioSemanal> horarios = List.of(horario(DiaSemana.LUNES, 8, 9));
        LocalDateTime martes = LUNES.plusDays(1).atTime(8, 0);
        List<TimeSlot> ocupadas = List.of(new TimeSlot(martes, martes.plusHours(1)));

        // Act
        List<TimeSlot> slots = calculator.calcularDisponibilidad(SOLO_LUNES, horarios, ocupadas, 60);

        // Assert
        assertEquals(List.of(franja(LUNES, 8, 0, 9, 0)), slots);
    }
}
