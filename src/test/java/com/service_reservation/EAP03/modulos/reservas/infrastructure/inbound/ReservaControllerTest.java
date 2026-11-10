package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Cancelacion;
import com.service_reservation.EAP03.modulos.reservas.domain.model.CancelarReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.CrearReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.IntervaloReservado;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PartesReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.CancelarReservaUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.in.CrearReservaUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaControllerTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 2, 9, 0);
    private static final LocalDateTime CANCELADA = LocalDateTime.of(2026, 10, 24, 12, 0);
    private static final Authentication CLIENTE = new UsernamePasswordAuthenticationToken(
            new UsuarioAutenticadoPrincipal(20L, "cliente@test.com", List.of("CLIENTE")), null, List.of());

    @Mock private CrearReservaUseCase crearReservaUseCase;
    @Mock private CancelarReservaUseCase cancelarReservaUseCase;
    @Mock private com.service_reservation.EAP03.modulos.reservas.domain.ports.in.ConsultarHistorialReservasUseCase consultarHistorialReservasUseCase;

    @InjectMocks private ReservaController controller;

    private static Reserva reserved() {
        return Reserva.reconstruir(50, new PartesReserva(3L, 10, 7), new IntervaloReservado(INICIO, INICIO.plusHours(1)),
                EstadoReserva.RESERVADO, "Nota", CANCELADA, null);
    }

    private static Reserva cancelled() {
        return Reserva.reconstruir(50, new PartesReserva(3L, 10, 7), new IntervaloReservado(INICIO, INICIO.plusHours(1)),
                EstadoReserva.CANCELADO, null, CANCELADA, new Cancelacion(CANCELADA, "Viaje"));
    }

    @Test
    @DisplayName("Crear pasa al caso de uso el usuario autenticado y los datos de la reserva")
    void createsReservationForAuthenticatedUser() {
        // Arrange
        when(crearReservaUseCase.ejecutar(new CrearReservaComando(20L, 10, INICIO, "Nota", List.of(4)))).thenReturn(reserved());

        // Act
        ReservaResponseDTO response = controller.crear(new CrearReservaRequestDTO(10, INICIO, "Nota", List.of(4)), CLIENTE);

        // Assert
        assertEquals(new ReservaResponseDTO(50, 10, INICIO, INICIO.plusHours(1), EstadoReserva.RESERVADO, "Nota"), response);
    }

    @Test
    @DisplayName("Sin un usuario autenticado del sistema el caso de uso recibe un usuario nulo")
    void passesNullUserWithoutAuthenticatedUser() {
        CrearReservaRequestDTO request = new CrearReservaRequestDTO(10, INICIO, null, null);
        when(crearReservaUseCase.ejecutar(new CrearReservaComando(null, 10, INICIO, null, null))).thenReturn(reserved());

        controller.crear(request, null);
        controller.crear(request, new UsernamePasswordAuthenticationToken("anonimo", null, List.of()));

        verify(crearReservaUseCase, times(2)).ejecutar(new CrearReservaComando(null, 10, INICIO, null, null));
    }

    @Test
    @DisplayName("Cancelar con motivo devuelve el resultado de la cancelación")
    void cancelsWithReason() {
        // Arrange
        when(cancelarReservaUseCase.ejecutar(new CancelarReservaComando(20L, 50, "Viaje"))).thenReturn(cancelled());

        // Act
        CancelacionReservaResponseDTO response = controller.cancelar(50, new CancelarReservaRequestDTO("Viaje"), CLIENTE);

        // Assert
        assertEquals(new CancelacionReservaResponseDTO(50, EstadoReserva.CANCELADO, INICIO, CANCELADA, "Viaje"), response);
    }

    @Test
    @DisplayName("Cancelar sin cuerpo envía un motivo nulo")
    void cancelsWithoutBody() {
        when(cancelarReservaUseCase.ejecutar(new CancelarReservaComando(20L, 50, null))).thenReturn(cancelled());

        controller.cancelar(50, null, CLIENTE);

        verify(cancelarReservaUseCase).ejecutar(new CancelarReservaComando(20L, 50, null));
    }

    @Test
    @DisplayName("Historial pasa al caso de uso el usuario autenticado y los parámetros validados")
    void historyUsesAuthenticatedUserAndParams() {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/reservas/mias");
        request.setParameter("estado", "CANCELADO");
        request.setParameter("page", "1");
        request.setParameter("size", "5");
        com.service_reservation.EAP03.modulos.reservas.domain.model.ConsultarHistorialComando comando =
                new com.service_reservation.EAP03.modulos.reservas.domain.model.ConsultarHistorialComando(20L, null, EstadoReserva.CANCELADO, 1, 5);
        when(consultarHistorialReservasUseCase.ejecutar(comando)).thenReturn(new com.service_reservation.EAP03.modulos.reservas.domain.model.PaginaHistorial(
                List.of(new com.service_reservation.EAP03.modulos.reservas.domain.model.ReservaHistorial(cancelled(), "Corte")), 1, 5, 6));

        HistorialReservasResponseDTO response = controller.historial(request, CLIENTE);

        assertEquals(1, response.contenido().size());
        assertEquals("Corte", response.contenido().get(0).nombreServicio());
        assertEquals(2, response.totalPaginas());
    }

    @Test
    @DisplayName("Historial con parámetros inválidos falla antes de llamar al caso de uso")
    void historyRejectsInvalidParams() {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/reservas/mias");
        request.setParameter("orden", "asc");

        org.junit.jupiter.api.Assertions.assertThrows(
                com.service_reservation.EAP03.modulos.reservas.domain.exception.ConsultaHistorialInvalidaException.class,
                () -> controller.historial(request, CLIENTE));
        org.mockito.Mockito.verifyNoInteractions(consultarHistorialReservasUseCase);
    }

    @Test
    @DisplayName("Historial sin usuario del sistema pasa un usuario nulo al caso de uso")
    void historyWithoutSystemUserPassesNull() {
        org.springframework.mock.web.MockHttpServletRequest request = new org.springframework.mock.web.MockHttpServletRequest("GET", "/api/v1/reservas/mias");
        when(consultarHistorialReservasUseCase.ejecutar(new com.service_reservation.EAP03.modulos.reservas.domain.model.ConsultarHistorialComando(null, null, null, 0, 10)))
                .thenReturn(new com.service_reservation.EAP03.modulos.reservas.domain.model.PaginaHistorial(List.of(), 0, 10, 0));

        assertEquals(HistorialReservasResponseDTO.MENSAJE_SIN_CITAS, controller.historial(request, null).mensaje());
    }
}
