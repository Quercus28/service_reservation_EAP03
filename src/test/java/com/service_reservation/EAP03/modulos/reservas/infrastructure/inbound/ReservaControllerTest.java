package com.service_reservation.EAP03.modulos.reservas.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.identidad.infrastructure.inbound.security.UsuarioAutenticadoPrincipal;
import com.service_reservation.EAP03.modulos.reservas.domain.model.CancelarReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.CrearReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
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

    @InjectMocks private ReservaController controller;

    private static Reserva reserved() {
        return Reserva.reconstruir(50, 3L, 10, 7, INICIO, INICIO.plusHours(1), EstadoReserva.RESERVADO, "Nota", CANCELADA, null, null);
    }

    private static Reserva cancelled() {
        return Reserva.reconstruir(50, 3L, 10, 7, INICIO, INICIO.plusHours(1), EstadoReserva.CANCELADO, null, CANCELADA, CANCELADA, "Viaje");
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
}
