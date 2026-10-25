package com.service_reservation.EAP03.modulos.agendas_horarios.application;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.AgendaNoEncontradaException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.DatosAgendaInvalidosException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.out.IPersistenciaAgendasHorarios;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestionHorariosServiceTest {

    private static final Horario LUNES = new Horario(1L, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(12, 0));

    @Mock private IPersistenciaAgendasHorarios persistencia;

    @InjectMocks private GestionHorariosService service;

    private static Agenda agenda() {
        return new Agenda(5L, 7, 10, true, List.of(LUNES));
    }

    @Test
    @DisplayName("Agrega el horario a la agenda propia y la guarda")
    void addsSchedule() {
        // Arrange
        when(persistencia.buscarPorIdYProveedor(5L, 7)).thenReturn(Optional.of(agenda()));
        when(persistencia.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Agenda resultado = service.agregarHorario(5L, 7, new Horario(DiaSemana.MARTES, LocalTime.of(8, 0), LocalTime.of(12, 0)));

        // Assert
        assertEquals(2, resultado.getHorarios().size());
    }

    @Test
    @DisplayName("Modifica un horario de la agenda propia y la guarda")
    void modifiesSchedule() {
        // Arrange
        when(persistencia.buscarPorIdYProveedor(5L, 7)).thenReturn(Optional.of(agenda()));
        when(persistencia.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Agenda resultado = service.modificarHorario(5L, 7, 1L, new Horario(DiaSemana.LUNES, LocalTime.of(9, 0), LocalTime.of(13, 0)));

        // Assert
        assertEquals(new Horario(1L, DiaSemana.LUNES, LocalTime.of(9, 0), LocalTime.of(13, 0)), resultado.getHorarios().get(0));
    }

    @Test
    @DisplayName("Modificar una agenda ajena o inexistente falla como no encontrada")
    void modifyUnknownAgenda() {
        when(persistencia.buscarPorIdYProveedor(5L, 7)).thenReturn(Optional.empty());

        assertThrows(AgendaNoEncontradaException.class, () -> service.modificarHorario(5L, 7, 1L, LUNES));
        verify(persistencia, never()).guardar(any());
    }

    @Test
    @DisplayName("Agregar o modificar exige la agenda y el proveedor")
    void changesRequireIds() {
        assertThrows(DatosAgendaInvalidosException.class, () -> service.agregarHorario(null, 7, LUNES));
        assertThrows(DatosAgendaInvalidosException.class, () -> service.agregarHorario(5L, null, LUNES));
        verifyNoInteractions(persistencia);
    }

    @Test
    @DisplayName("Devuelve los horarios de la agenda activa del servicio del proveedor")
    void returnsAvailableSchedules() {
        when(persistencia.buscarAgendaActivaPorServicio(10)).thenReturn(Optional.of(agenda()));

        assertEquals(List.of(LUNES), service.consultarHorariosDisponibles(7, 10));
    }

    @Test
    @DisplayName("No devuelve horarios de la agenda de otro proveedor ni de un servicio sin agenda")
    void noSchedulesForOtherProviderOrWithoutAgenda() {
        when(persistencia.buscarAgendaActivaPorServicio(10)).thenReturn(Optional.of(agenda()));
        when(persistencia.buscarAgendaActivaPorServicio(11)).thenReturn(Optional.empty());

        AgendaNoEncontradaException ex = assertThrows(AgendaNoEncontradaException.class,
                () -> service.consultarHorariosDisponibles(99, 10));
        assertEquals("No existe una agenda activa con id 10 para el proveedor autenticado.", ex.getMessage());
        assertThrows(AgendaNoEncontradaException.class, () -> service.consultarHorariosDisponibles(7, 11));
    }

    @Test
    @DisplayName("Consultar disponibilidad exige proveedor y servicio")
    void availabilityRequiresIds() {
        assertThrows(DatosAgendaInvalidosException.class, () -> service.consultarHorariosDisponibles(null, 10));
        assertThrows(DatosAgendaInvalidosException.class, () -> service.consultarHorariosDisponibles(7, null));
        verifyNoInteractions(persistencia);
    }
}
