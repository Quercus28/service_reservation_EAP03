package com.service_reservation.EAP03.modulos.agendas_horarios.domain.model;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.DatosAgendaInvalidosException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.HorarioSolapadoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AgendaTest {

    private static Horario schedule(Long id, DiaSemana dia, int inicio, int fin) {
        return new Horario(id, dia, LocalTime.of(inicio, 0), LocalTime.of(fin, 0));
    }

    private static Agenda agendaWith(Horario... horarios) {
        return new Agenda(5L, 7, 10, true, List.of(horarios));
    }

    @Test
    @DisplayName("Una agenda nueva queda activa, sin id y sin horarios")
    void newAgendaIsActiveAndEmpty() {
        Agenda agenda = new Agenda(7, 10);

        assertNull(agenda.getId());
        assertEquals(7, agenda.getProveedorId());
        assertEquals(10, agenda.getServicioId());
        assertTrue(agenda.isActiva());
        assertTrue(agenda.getHorarios().isEmpty());
    }

    @Test
    @DisplayName("Reconstruye una agenda con sus horarios")
    void rebuildsAgendaWithSchedules() {
        Horario lunes = schedule(1L, DiaSemana.LUNES, 8, 12);

        Agenda agenda = new Agenda(5L, 7, 10, false, List.of(lunes));

        assertEquals(5L, agenda.getId());
        assertFalse(agenda.isActiva());
        assertEquals(List.of(lunes), agenda.getHorarios());
    }

    @Test
    @DisplayName("Horarios nulos se convierten en una agenda sin horarios")
    void nullSchedulesBecomeEmpty() {
        assertTrue(new Agenda(5L, 7, 10, true, null).getHorarios().isEmpty());
    }

    @Test
    @DisplayName("Rechaza una agenda sin proveedor o sin servicio")
    void rejectsMissingProviderOrService() {
        assertThrows(NullPointerException.class, () -> new Agenda(null, 10));
        assertThrows(NullPointerException.class, () -> new Agenda(7, null));
    }

    @Test
    @DisplayName("Los horarios expuestos no se pueden modificar")
    void schedulesAreUnmodifiable() {
        Agenda agenda = agendaWith(schedule(1L, DiaSemana.LUNES, 8, 12));

        List<Horario> horarios = agenda.getHorarios();
        assertThrows(UnsupportedOperationException.class, () -> horarios.clear());
    }

    @Test
    @DisplayName("Agrega un horario que no se superpone")
    void addsNonOverlappingSchedule() {
        Agenda agenda = agendaWith(schedule(1L, DiaSemana.LUNES, 8, 12));

        agenda.agregarHorario(schedule(null, DiaSemana.LUNES, 12, 16));

        assertEquals(2, agenda.getHorarios().size());
    }

    @Test
    @DisplayName("Rechaza agregar un horario nulo o que se superpone")
    void rejectsNullOrOverlappingSchedule() {
        Agenda agenda = agendaWith(schedule(1L, DiaSemana.LUNES, 8, 12));

        assertThrows(NullPointerException.class, () -> agenda.agregarHorario(null));
        Horario solapado = schedule(null, DiaSemana.LUNES, 11, 13);
        HorarioSolapadoException ex = assertThrows(HorarioSolapadoException.class,
                () -> agenda.agregarHorario(solapado));
        assertEquals("El horario se superpone con un horario previamente registrado en la agenda", ex.getMessage());
    }

    @Test
    @DisplayName("Reconstruir una agenda con horarios superpuestos falla")
    void rejectsRebuildingWithOverlaps() {
        Horario lunes = schedule(1L, DiaSemana.LUNES, 8, 12);
        Horario solapado = schedule(2L, DiaSemana.LUNES, 10, 14);
        assertThrows(HorarioSolapadoException.class, () -> agendaWith(lunes, solapado));
    }

    @Test
    @DisplayName("Reemplaza un horario conservando su id, aunque se cruce con su versión anterior")
    void replacesScheduleKeepingId() {
        // Arrange
        Agenda agenda = agendaWith(schedule(1L, DiaSemana.LUNES, 8, 12), schedule(2L, DiaSemana.MARTES, 8, 12));

        // Act
        agenda.reemplazarHorario(2L, schedule(null, DiaSemana.MARTES, 9, 13));

        // Assert
        assertEquals(schedule(2L, DiaSemana.MARTES, 9, 13), agenda.getHorarios().get(1));
        assertEquals(schedule(1L, DiaSemana.LUNES, 8, 12), agenda.getHorarios().get(0));
    }

    @Test
    @DisplayName("Rechaza reemplazar por un horario que se superpone con otro")
    void rejectsReplacementOverlappingAnother() {
        Agenda agenda = agendaWith(schedule(1L, DiaSemana.LUNES, 8, 12), schedule(2L, DiaSemana.LUNES, 14, 16));

        Horario solapado = schedule(null, DiaSemana.LUNES, 11, 15);
        HorarioSolapadoException ex = assertThrows(HorarioSolapadoException.class,
                () -> agenda.reemplazarHorario(2L, solapado));

        assertEquals("El nuevo horario se superpone con otro horario previamente registrado para el mismo día.", ex.getMessage());
    }

    @Test
    @DisplayName("Rechaza reemplazar un horario que no existe en la agenda")
    void rejectsReplacingUnknownSchedule() {
        Agenda agenda = agendaWith(schedule(1L, DiaSemana.LUNES, 8, 12));

        Horario lunes = schedule(null, DiaSemana.LUNES, 8, 12);
        DatosAgendaInvalidosException ex = assertThrows(DatosAgendaInvalidosException.class,
                () -> agenda.reemplazarHorario(9L, lunes));

        assertEquals("El horario indicado no existe en la agenda", ex.getMessage());
    }

    @Test
    @DisplayName("Reemplazar exige el id y el nuevo horario")
    void replaceRequiresIdAndSchedule() {
        Agenda agenda = agendaWith(schedule(1L, DiaSemana.LUNES, 8, 12));

        Horario lunes = schedule(null, DiaSemana.LUNES, 8, 12);
        assertThrows(NullPointerException.class, () -> agenda.reemplazarHorario(null, lunes));
        assertThrows(NullPointerException.class, () -> agenda.reemplazarHorario(1L, null));
    }
}
