package com.service_reservation.EAP03.modulos.recursos.domain.model;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.DatosInvalidosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class HorarioRecursoTest {

    private static final LocalTime OCHO = LocalTime.of(8, 0);
    private static final LocalTime DOCE = LocalTime.of(12, 0);
    private static final LocalDateTime LUNES_NUEVE = LocalDateTime.of(2026, 10, 5, 9, 0);

    private static HorarioRecurso lunesDeOchoADoce() {
        return HorarioRecurso.nuevo(1, DiaSemana.LUNES, OCHO, DOCE);
    }

    @Test
    @DisplayName("Crea un horario nuevo sin id")
    void createsNewSchedule() {
        HorarioRecurso horario = lunesDeOchoADoce();

        assertNull(horario.getId());
        assertEquals(1, horario.getIdRecurso());
        assertEquals(DiaSemana.LUNES, horario.getDiaSemana());
        assertEquals(OCHO, horario.getHoraInicio());
        assertEquals(DOCE, horario.getHoraFin());
    }

    @Test
    @DisplayName("Reconstruye un horario existente con su id")
    void rebuildsSchedule() {
        assertEquals(5, HorarioRecurso.reconstruir(5, 1, DiaSemana.MARTES, OCHO, DOCE).getId());
    }

    @Test
    @DisplayName("Rechaza horas nulas")
    void rejectsNullHours() {
        DatosInvalidosException ex = assertThrows(DatosInvalidosException.class,
                () -> HorarioRecurso.nuevo(1, DiaSemana.LUNES, null, DOCE));

        assertEquals("horaInicio y horaFin son obligatorias", ex.getMessage());
        assertThrows(DatosInvalidosException.class, () -> HorarioRecurso.nuevo(1, DiaSemana.LUNES, OCHO, null));
    }

    @Test
    @DisplayName("Rechaza una hora de fin que no sea posterior al inicio")
    void rejectsEndNotAfterStart() {
        DatosInvalidosException ex = assertThrows(DatosInvalidosException.class,
                () -> HorarioRecurso.nuevo(1, DiaSemana.LUNES, DOCE, OCHO));

        assertEquals("horaFin debe ser posterior a horaInicio", ex.getMessage());
    }

    @Test
    @DisplayName("Rechaza un horario sin recurso o sin día")
    void rejectsNullResourceOrDay() {
        assertThrows(NullPointerException.class, () -> HorarioRecurso.nuevo(null, DiaSemana.LUNES, OCHO, DOCE));
        assertThrows(NullPointerException.class, () -> HorarioRecurso.nuevo(1, null, OCHO, DOCE));
    }

    @Test
    @DisplayName("Se solapa con una franja del mismo día que se cruza")
    void overlapsSameDay() {
        assertTrue(lunesDeOchoADoce().seSolapaCon(HorarioRecurso.nuevo(1, DiaSemana.LUNES, LocalTime.of(11, 0), LocalTime.of(14, 0))));
        assertTrue(lunesDeOchoADoce().seSolapaCon(HorarioRecurso.nuevo(1, DiaSemana.LUNES, LocalTime.of(6, 0), LocalTime.of(9, 0))));
    }

    @Test
    @DisplayName("No se solapa con otro día ni con franjas que solo se tocan en el borde")
    void doesNotOverlap() {
        assertFalse(lunesDeOchoADoce().seSolapaCon(HorarioRecurso.nuevo(1, DiaSemana.MARTES, OCHO, DOCE)));
        assertFalse(lunesDeOchoADoce().seSolapaCon(HorarioRecurso.nuevo(1, DiaSemana.LUNES, DOCE, LocalTime.of(16, 0))));
        assertFalse(lunesDeOchoADoce().seSolapaCon(HorarioRecurso.nuevo(1, DiaSemana.LUNES, LocalTime.of(6, 0), OCHO)));
    }

    @Test
    @DisplayName("Contiene un rango del mismo día dentro de la franja, incluidos los bordes")
    void containsRangeInside() {
        assertTrue(lunesDeOchoADoce().contiene(LUNES_NUEVE, LUNES_NUEVE.plusHours(1)));
        assertTrue(lunesDeOchoADoce().contiene(LUNES_NUEVE.withHour(8), LUNES_NUEVE.withHour(12)));
    }

    @Test
    @DisplayName("No contiene rangos nulos, vacíos o invertidos")
    void doesNotContainInvalidRanges() {
        assertFalse(lunesDeOchoADoce().contiene(null, LUNES_NUEVE));
        assertFalse(lunesDeOchoADoce().contiene(LUNES_NUEVE, null));
        assertFalse(lunesDeOchoADoce().contiene(LUNES_NUEVE, LUNES_NUEVE));
        assertFalse(lunesDeOchoADoce().contiene(LUNES_NUEVE.plusHours(1), LUNES_NUEVE));
    }

    @Test
    @DisplayName("No contiene rangos que cruzan de día o que caen en otro día de la semana")
    void doesNotContainOtherDays() {
        assertFalse(lunesDeOchoADoce().contiene(LUNES_NUEVE, LUNES_NUEVE.plusDays(1)));
        assertFalse(lunesDeOchoADoce().contiene(LUNES_NUEVE.plusDays(1), LUNES_NUEVE.plusDays(1).plusHours(1)));
    }

    @Test
    @DisplayName("No contiene rangos que empiezan antes o terminan después de la franja")
    void doesNotContainRangesOutsideHours() {
        assertFalse(lunesDeOchoADoce().contiene(LUNES_NUEVE.withHour(7), LUNES_NUEVE));
        assertFalse(lunesDeOchoADoce().contiene(LUNES_NUEVE, LUNES_NUEVE.withHour(13)));
    }
}
