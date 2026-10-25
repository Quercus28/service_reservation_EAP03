package com.service_reservation.EAP03.modulos.agendas_horarios.application;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.AgendaDuplicadaException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.AgendaNoEncontradaException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.DatosAgendaInvalidosException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.ServicioNoPerteneceAlProveedorException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.out.IPersistenciaAgendasHorarios;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ObtenerServicioPorIdUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestionAgendasServiceTest {

    @Mock private IPersistenciaAgendasHorarios persistencia;
    @Mock private ObtenerServicioPorIdUseCase obtenerServicioPorId;

    @InjectMocks private GestionAgendasService service;

    private void serviceOwnedBy(Integer idProveedor) {
        when(obtenerServicioPorId.ejecutar(10)).thenReturn(new ServicioResponse(10, idProveedor, "Masaje", 60, null));
    }

    @Test
    @DisplayName("Crea una agenda activa para un servicio propio sin agenda")
    void createsAgenda() {
        // Arrange
        serviceOwnedBy(7);
        when(persistencia.existeAgendaActivaPorServicio(10)).thenReturn(false);
        when(persistencia.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Agenda agenda = service.crearAgenda(7, 10);

        // Assert
        assertEquals(7, agenda.getProveedorId());
        assertEquals(10, agenda.getServicioId());
        assertTrue(agenda.isActiva());
    }

    @Test
    @DisplayName("Crear exige proveedor y servicio")
    void createRequiresIds() {
        DatosAgendaInvalidosException sinProveedor = assertThrows(DatosAgendaInvalidosException.class,
                () -> service.crearAgenda(null, 10));
        DatosAgendaInvalidosException sinServicio = assertThrows(DatosAgendaInvalidosException.class,
                () -> service.crearAgenda(7, null));

        assertEquals("El proveedor es obligatorio.", sinProveedor.getMessage());
        assertEquals("El servicio es obligatorio.", sinServicio.getMessage());
        verifyNoInteractions(obtenerServicioPorId, persistencia);
    }

    @Test
    @DisplayName("Rechaza crear la agenda de un servicio de otro proveedor o sin proveedor")
    void rejectsServiceOfAnotherProvider() {
        serviceOwnedBy(99);
        assertThrows(ServicioNoPerteneceAlProveedorException.class, () -> service.crearAgenda(7, 10));

        reset(obtenerServicioPorId);
        serviceOwnedBy(null);
        assertThrows(ServicioNoPerteneceAlProveedorException.class, () -> service.crearAgenda(7, 10));
        verifyNoInteractions(persistencia);
    }

    @Test
    @DisplayName("Rechaza crear una segunda agenda activa para el servicio")
    void rejectsDuplicatedAgenda() {
        serviceOwnedBy(7);
        when(persistencia.existeAgendaActivaPorServicio(10)).thenReturn(true);

        assertThrows(AgendaDuplicadaException.class, () -> service.crearAgenda(7, 10));
        verify(persistencia, never()).guardar(any());
    }

    @Test
    @DisplayName("Consulta la agenda del proveedor")
    void queriesAgenda() {
        Agenda agenda = new Agenda(5L, 7, 10, true, List.of());
        when(persistencia.buscarPorIdYProveedor(5L, 7)).thenReturn(Optional.of(agenda));

        assertSame(agenda, service.consultarAgenda(5L, 7));
    }

    @Test
    @DisplayName("Consultar una agenda ajena o inexistente falla como no encontrada")
    void queryUnknownAgenda() {
        when(persistencia.buscarPorIdYProveedor(5L, 7)).thenReturn(Optional.empty());

        AgendaNoEncontradaException ex = assertThrows(AgendaNoEncontradaException.class, () -> service.consultarAgenda(5L, 7));

        assertEquals("No existe una agenda activa con id 5 para el proveedor autenticado.", ex.getMessage());
    }

    @Test
    @DisplayName("Consultar exige la agenda y el proveedor")
    void queryRequiresIds() {
        assertThrows(DatosAgendaInvalidosException.class, () -> service.consultarAgenda(null, 7));
        assertThrows(DatosAgendaInvalidosException.class, () -> service.consultarAgenda(5L, null));
        verifyNoInteractions(persistencia);
    }
}
