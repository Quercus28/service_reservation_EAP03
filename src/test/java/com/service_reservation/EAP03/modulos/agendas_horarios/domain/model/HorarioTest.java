package com.service_reservation.EAP03.modulos.agendas_horarios.domain.model;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.DatosAgendaInvalidosException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class HorarioTest {

    private static final LocalTime OCHO = LocalTime.of(8, 0);
    private static final LocalTime DOCE = LocalTime.of(12, 0);

    private static Horario monday(int inicio, int fin) {
        return new Horario(DiaSemana.LUNES, LocalTime.of(inicio, 0), LocalTime.of(fin, 0));
    }

    @Test
    @DisplayName("Crea un horario sin id con el constructor corto")
    void createsScheduleWithoutId() {
        Horario horario = new Horario(DiaSemana.LUNES, OCHO, DOCE);

        assertNull(horario.id());
        assertEquals(DiaSemana.LUNES, horario.diaSemana());
        assertEquals(OCHO, horario.horaInicio());
        assertEquals(DOCE, horario.horaFin());
    }

    @Test
    @DisplayName("Rechaza día, hora de inicio u hora de fin nulos")
    void rejectsNullValues() {
        assertThrows(NullPointerException.class, () -> new Horario(null, OCHO, DOCE));
        assertThrows(NullPointerException.class, () -> new Horario(DiaSemana.LUNES, null, DOCE));
        assertThrows(NullPointerException.class, () -> new Horario(DiaSemana.LUNES, OCHO, null));
    }

    @Test
    @DisplayName("Rechaza una hora de fin que no sea posterior a la de inicio")
    void rejectsEndNotAfterStart() {
        DatosAgendaInvalidosException ex = assertThrows(DatosAgendaInvalidosException.class,
                () -> new Horario(1L, DiaSemana.LUNES, DOCE, OCHO));

        assertEquals("La hora de finalización debe ser posterior a la hora de inicio", ex.getMessage());
        assertThrows(DatosAgendaInvalidosException.class, () -> new Horario(DiaSemana.LUNES, OCHO, OCHO));
    }

    @Test
    @DisplayName("Se superpone con otro horario del mismo día que se cruza")
    void overlapsSameDay() {
        assertTrue(monday(8, 12).seSuperponeCon(monday(11, 14)));
        assertTrue(monday(8, 12).seSuperponeCon(monday(6, 9)));
    }

    @Test
    @DisplayName("No se superpone con nulo, con otro día ni con horarios que solo se tocan en el borde")
    void doesNotOverlap() {
        assertFalse(monday(8, 12).seSuperponeCon(null));
        assertFalse(monday(8, 12).seSuperponeCon(new Horario(DiaSemana.MARTES, OCHO, DOCE)));
        assertFalse(monday(8, 12).seSuperponeCon(monday(12, 16)));
        assertFalse(monday(8, 12).seSuperponeCon(monday(6, 8)));
    }
}
