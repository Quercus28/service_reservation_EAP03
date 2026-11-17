package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarClienteUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.HistorialAjenoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.UsuarioNoEsClienteException;
import com.service_reservation.EAP03.modulos.reservas.domain.model.ConsultarHistorialComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.IntervaloReservado;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PaginaHistorial;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PaginaReservas;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PartesReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.out.ReservaRepositoryPort;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.dto.ServicioResponse;
import com.service_reservation.EAP03.modulos.servicios_catalogo.application.usecase.ObtenerServicioPorIdUseCase;
import com.service_reservation.EAP03.modulos.servicios_catalogo.domain.exception.ServicioNoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarHistorialReservasServiceTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 11, 2, 9, 0);

    @Mock private ConsultarClienteUseCase consultarClienteUseCase;
    @Mock private ReservaRepositoryPort reservaRepositoryPort;
    @Mock private ObtenerServicioPorIdUseCase obtenerServicioPorIdUseCase;

    @InjectMocks private ConsultarHistorialReservasService service;

    private static Reserva reserva(int id, int idServicio, EstadoReserva estado) {
        return Reserva.reconstruir(id, new PartesReserva(3L, idServicio, 7),
                new IntervaloReservado(INICIO, INICIO.plusHours(1)), estado, null, INICIO.minusDays(5), null);
    }

    @Test
    @DisplayName("Devuelve las reservas del cliente del usuario, en cualquier estado, con el nombre del servicio")
    void returnsOwnHistoryWithAnyState() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(20L)).thenReturn(Optional.of(3L));
        when(reservaRepositoryPort.buscarHistorialDeCliente(3L, null, 0, 10)).thenReturn(new PaginaReservas(
                List.of(reserva(1, 10, EstadoReserva.CANCELADO), reserva(2, 10, EstadoReserva.COMPLETADO)), 0, 10, 2));
        when(obtenerServicioPorIdUseCase.ejecutar(10)).thenReturn(new ServicioResponse(10, 7, "Corte", 60, null));

        PaginaHistorial r = service.ejecutar(new ConsultarHistorialComando(20L, null, null, 0, 10));

        assertEquals(2, r.total());
        assertEquals(EstadoReserva.CANCELADO, r.contenido().get(0).reserva().getEstado());
        assertEquals("Corte", r.contenido().get(1).nombreServicio());
        verify(obtenerServicioPorIdUseCase, times(1)).ejecutar(10);
    }

    @Test
    @DisplayName("Un cliente sin reservas recibe una página vacía")
    void returnsEmptyPage() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(20L)).thenReturn(Optional.of(3L));
        when(reservaRepositoryPort.buscarHistorialDeCliente(3L, EstadoReserva.CANCELADO, 0, 10))
                .thenReturn(new PaginaReservas(List.of(), 0, 10, 0));

        PaginaHistorial r = service.ejecutar(new ConsultarHistorialComando(20L, 3L, EstadoReserva.CANCELADO, 0, 10));

        assertEquals(0, r.total());
        assertEquals(0, r.contenido().size());
    }

    @Test
    @DisplayName("Si el servicio ya no existe, la cita se conserva sin nombre")
    void keepsReservationWhenServiceIsGone() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(20L)).thenReturn(Optional.of(3L));
        when(reservaRepositoryPort.buscarHistorialDeCliente(3L, null, 0, 10))
                .thenReturn(new PaginaReservas(List.of(reserva(1, 99, EstadoReserva.NO_ASISTIO)), 0, 10, 1));
        when(obtenerServicioPorIdUseCase.ejecutar(99)).thenThrow(new ServicioNoEncontradoException(99));

        assertNull(service.ejecutar(new ConsultarHistorialComando(20L, null, null, 0, 10)).contenido().get(0).nombreServicio());
    }

    @Test
    @DisplayName("Pedir el historial de otro cliente se bloquea sin consultar reservas")
    void blocksOtherClientsHistory() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(20L)).thenReturn(Optional.of(3L));

        assertThrows(HistorialAjenoException.class,
                () -> service.ejecutar(new ConsultarHistorialComando(20L, 4L, null, 0, 10)));
        verifyNoInteractions(reservaRepositoryPort);
    }

    @Test
    @DisplayName("Un usuario que no es cliente no puede consultar")
    void rejectsNonClient() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(20L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEsClienteException.class,
                () -> service.ejecutar(new ConsultarHistorialComando(20L, null, null, 0, 10)));
        assertThrows(UsuarioNoEsClienteException.class,
                () -> service.ejecutar(new ConsultarHistorialComando(null, null, null, 0, 10)));
        verify(reservaRepositoryPort, never()).buscarHistorialDeCliente(any(), any(), org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());
    }
}