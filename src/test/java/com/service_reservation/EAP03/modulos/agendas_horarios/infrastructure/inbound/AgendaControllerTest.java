package com.service_reservation.EAP03.modulos.agendas_horarios.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.agendas_horarios.domain.exception.DatosAgendaInvalidosException;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Agenda;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.DiaSemana;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.model.Horario;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IDisponibilidad;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IGestionAgendas;
import com.service_reservation.EAP03.modulos.agendas_horarios.domain.ports.in.IGestionHorarios;
import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgendaControllerTest {

    private static final Authentication PROVEEDOR = new UsernamePasswordAuthenticationToken(
            new UsuarioAutenticadoPrincipal(20L, "prov@test.com", List.of("PROVEEDOR")), null, List.of());
    private static final Horario LUNES = new Horario(1L, DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(12, 0));
    private static final AgendaHorarioRequestDTO REQUEST = new AgendaHorarioRequestDTO(DiaSemana.LUNES, "08:00", "12:00");

    @Mock private IGestionAgendas gestionAgendas;
    @Mock private IGestionHorarios gestionHorarios;
    @Mock private IDisponibilidad disponibilidad;
    @Mock private ConsultarProveedorUseCase consultarProveedor;

    @InjectMocks private AgendaController controller;

    private static Agenda agenda() {
        return new Agenda(5L, 7, 10, true, List.of(LUNES));
    }

    private static AgendaResponseDTO expectedResponse() {
        return new AgendaResponseDTO(5L, 7, 10, true,
                List.of(new AgendaResponseDTO.HorarioResponseDTO(1L, "LUNES", LocalTime.of(8, 0), LocalTime.of(12, 0))));
    }

    private void authenticatedProvider() {
        when(consultarProveedor.buscarIdProveedorPorUsuario(20L)).thenReturn(Optional.of(7));
    }

    @Test
    @DisplayName("Crear agenda responde 201 con la agenda del proveedor autenticado")
    void createsAgenda() {
        authenticatedProvider();
        when(gestionAgendas.crearAgenda(7, 10)).thenReturn(agenda());

        ResponseEntity<AgendaResponseDTO> response = controller.crearAgenda(new CrearAgendaRequestDTO(10), PROVEEDOR);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(expectedResponse(), response.getBody());
    }

    @Test
    @DisplayName("Consultar agenda responde 200 con la agenda")
    void queriesAgenda() {
        authenticatedProvider();
        when(gestionAgendas.consultarAgenda(5L, 7)).thenReturn(agenda());

        ResponseEntity<AgendaResponseDTO> response = controller.consultarAgenda(5L, PROVEEDOR);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse(), response.getBody());
    }

    @Test
    @DisplayName("Agregar horario convierte las horas y responde 201")
    void addsSchedule() {
        authenticatedProvider();
        when(gestionHorarios.agregarHorario(5L, 7, new Horario(DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(12, 0))))
                .thenReturn(agenda());

        ResponseEntity<AgendaResponseDTO> response = controller.agregarHorario(5L, REQUEST, PROVEEDOR);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(expectedResponse(), response.getBody());
    }

    @Test
    @DisplayName("Modificar horario convierte las horas y responde 200")
    void modifiesSchedule() {
        authenticatedProvider();
        when(gestionHorarios.modificarHorario(5L, 7, 1L, new Horario(DiaSemana.LUNES, LocalTime.of(8, 0), LocalTime.of(12, 0))))
                .thenReturn(agenda());

        ResponseEntity<AgendaResponseDTO> response = controller.modificarHorario(5L, 1L, REQUEST, PROVEEDOR);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse(), response.getBody());
    }

    @Test
    @DisplayName("Consultar disponibilidad devuelve los horarios del servicio")
    void queriesAvailability() {
        authenticatedProvider();
        when(disponibilidad.consultarHorariosDisponibles(7, 10)).thenReturn(List.of(LUNES));

        assertEquals(List.of(LUNES), controller.consultarDisponibilidad(10, PROVEEDOR).getBody());
    }

    @Test
    @DisplayName("Falla sin un usuario autenticado del sistema")
    void failsWithoutAuthenticatedUser() {
        Authentication otroPrincipal = new UsernamePasswordAuthenticationToken("anonimo", null, List.of());

        DatosAgendaInvalidosException ex = assertThrows(DatosAgendaInvalidosException.class,
                () -> controller.consultarAgenda(5L, null));
        assertEquals("El usuario autenticado es obligatorio.", ex.getMessage());
        assertThrows(DatosAgendaInvalidosException.class, () -> controller.consultarAgenda(5L, otroPrincipal));
        verifyNoInteractions(consultarProveedor, gestionAgendas);
    }

    @Test
    @DisplayName("Falla si el usuario autenticado no es proveedor")
    void failsWhenUserIsNotProvider() {
        when(consultarProveedor.buscarIdProveedorPorUsuario(20L)).thenReturn(Optional.empty());

        DatosAgendaInvalidosException ex = assertThrows(DatosAgendaInvalidosException.class,
                () -> controller.consultarAgenda(5L, PROVEEDOR));

        assertEquals("El usuario autenticado no corresponde a un proveedor.", ex.getMessage());
        verifyNoInteractions(gestionAgendas);
    }
}
