package com.service_reservation.EAP03.modulos.availability.domain.model;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.availability.domain.exception.DisponibilidadInvalidaException;
import com.service_reservation.EAP03.modulos.availability.domain.exception.ServicioParaDisponibilidadNoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ValueObjectsDisponibilidadTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 2);
    private static final LocalTime OCHO = LocalTime.of(8, 0);
    private static final LocalTime DOCE = LocalTime.of(12, 0);

    // ---- DateRange ----

    @Test
    @DisplayName("DateRange de un solo día cuenta 1 día")
    void dateRangeMismoDia() {
        assertEquals(1, new DateRange(HOY, HOY).totalDias());
    }

    @Test
    @DisplayName("DateRange cuenta ambos extremos del rango")
    void dateRangeIncluyeExtremos() {
        assertEquals(30, new DateRange(HOY, HOY.plusDays(29)).totalDias());
    }

    @Test
    @DisplayName("DateRange rechaza fechas nulas")
    void dateRangeRechazaNulos() {
        assertThrows(NullPointerException.class, () -> new DateRange(null, HOY));
        assertThrows(NullPointerException.class, () -> new DateRange(HOY, null));
    }

    @Test
    @DisplayName("DateRange rechaza endDate anterior a startDate")
    void dateRangeRechazaFinAnterior() {
        assertThrows(DisponibilidadInvalidaException.class, () -> new DateRange(HOY, HOY.minusDays(1)));
    }

    // ---- HorarioSemanal ----

    @Test
    @DisplayName("HorarioSemanal válido conserva sus datos")
    void horarioSemanalValido() {
        HorarioSemanal horario = new HorarioSemanal(DiaSemana.LUNES, OCHO, DOCE);

        assertEquals(DiaSemana.LUNES, horario.diaSemana());
        assertEquals(OCHO, horario.horaInicio());
        assertEquals(DOCE, horario.horaFin());
    }

    @Test
    @DisplayName("HorarioSemanal rechaza campos nulos")
    void horarioSemanalRechazaNulos() {
        assertThrows(NullPointerException.class, () -> new HorarioSemanal(null, OCHO, DOCE));
        assertThrows(NullPointerException.class, () -> new HorarioSemanal(DiaSemana.LUNES, null, DOCE));
        assertThrows(NullPointerException.class, () -> new HorarioSemanal(DiaSemana.LUNES, OCHO, null));
    }

    @Test
    @DisplayName("HorarioSemanal rechaza horaFin igual o anterior a horaInicio")
    void horarioSemanalRechazaFinNoPosterior() {
        assertThrows(DisponibilidadInvalidaException.class, () -> new HorarioSemanal(DiaSemana.LUNES, OCHO, OCHO));
        assertThrows(DisponibilidadInvalidaException.class, () -> new HorarioSemanal(DiaSemana.LUNES, DOCE, OCHO));
    }

    // ---- ServicioDisponibilidadInfo ----

    @Test
    @DisplayName("ServicioDisponibilidadInfo válido conserva sus datos")
    void servicioInfoValido() {
        ServicioDisponibilidadInfo info = new ServicioDisponibilidadInfo(10, 7, 45);

        assertEquals(10, info.id());
        assertEquals(7, info.idProveedor());
        assertEquals(45, info.duracionMinutos());
    }

    @Test
    @DisplayName("ServicioDisponibilidadInfo rechaza ids nulos")
    void servicioInfoRechazaNulos() {
        assertThrows(NullPointerException.class, () -> new ServicioDisponibilidadInfo(null, 7, 45));
        assertThrows(NullPointerException.class, () -> new ServicioDisponibilidadInfo(10, null, 45));
    }

    @Test
    @DisplayName("ServicioDisponibilidadInfo rechaza duración cero o negativa")
    void servicioInfoRechazaDuracionInvalida() {
        assertThrows(IllegalArgumentException.class, () -> new ServicioDisponibilidadInfo(10, 7, 0));
        assertThrows(IllegalArgumentException.class, () -> new ServicioDisponibilidadInfo(10, 7, -15));
    }

    // ---- Excepciones ----

    @Test
    @DisplayName("ServicioParaDisponibilidadNoEncontradoException incluye el id en el mensaje")
    void mensajeServicioNoEncontrado() {
        assertEquals("Servicio con ID 99 no fue encontrado o no está disponible",
                new ServicioParaDisponibilidadNoEncontradoException(99).getMessage());
    }
}
