package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarClienteUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.LiberarRecursosDeReservaUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.CancelacionFueraDeTiempoException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.DatosReservaInvalidosException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaAjenaException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.ReservaNoEncontradaException;
import com.service_reservation.EAP03.modulos.reservas.domain.exception.UsuarioNoEsClienteException;
import com.service_reservation.EAP03.modulos.reservas.domain.model.CancelarReservaComando;
import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.out.ReservaRepositoryPort;
import com.service_reservation.EAP03.modulos.reservas.infrastructure.config.ReservaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelarReservaServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 24, 12, 0);
    private static final CancelarReservaComando COMANDO = new CancelarReservaComando(20L, 8, "Viaje");

    @Mock private ConsultarClienteUseCase consultarClienteUseCase;
    @Mock private ReservaRepositoryPort reservaRepositoryPort;
    @Mock private LiberarRecursosDeReservaUseCase liberarRecursosDeReservaUseCase;

    private CancelarReservaService service;

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(Instant.parse("2026-10-24T12:00:00Z"), ZoneOffset.UTC);
        service = new CancelarReservaService(consultarClienteUseCase, reservaRepositoryPort, liberarRecursosDeReservaUseCase,
                new ReservaProperties(2), reloj);
    }

    private static Reserva reservationOf(Long idCliente, LocalDateTime inicio) {
        return Reserva.reconstruir(8, idCliente, 10, 7, inicio, inicio.plusHours(1), EstadoReserva.RESERVADO,
                null, AHORA.minusDays(1), null, null);
    }

    @Test
    @DisplayName("Cancela la reserva propia, la guarda y libera sus recursos")
    void cancelsOwnReservation() {
        // Arrange
        when(consultarClienteUseCase.buscarIdClientePorUsuario(20L)).thenReturn(Optional.of(3L));
        when(reservaRepositoryPort.buscarPorId(8)).thenReturn(Optional.of(reservationOf(3L, AHORA.plusDays(1))));
        when(reservaRepositoryPort.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Reserva cancelada = service.ejecutar(COMANDO);

        // Assert
        assertEquals(EstadoReserva.CANCELADO, cancelada.getEstado());
        assertEquals(AHORA, cancelada.getCanceladaEn());
        assertEquals("Viaje", cancelada.getMotivoCancelacion());
        verify(liberarRecursosDeReservaUseCase).ejecutar(8);
    }

    @Test
    @DisplayName("Respeta la anticipación mínima configurada")
    void appliesConfiguredMinimumNotice() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(20L)).thenReturn(Optional.of(3L));
        when(reservaRepositoryPort.buscarPorId(8)).thenReturn(Optional.of(reservationOf(3L, AHORA.plusMinutes(90))));

        assertThrows(CancelacionFueraDeTiempoException.class, () -> service.ejecutar(COMANDO));
        verify(reservaRepositoryPort, never()).guardar(any());
        verifyNoInteractions(liberarRecursosDeReservaUseCase);
    }

    @Test
    @DisplayName("Exige el identificador de la reserva")
    void requiresReservationId() {
        CancelarReservaComando sinReserva = new CancelarReservaComando(20L, null, null);
        DatosReservaInvalidosException ex = assertThrows(DatosReservaInvalidosException.class,
                () -> service.ejecutar(sinReserva));

        assertEquals("El identificador de la reserva es obligatorio", ex.getMessage());
        verifyNoInteractions(consultarClienteUseCase, reservaRepositoryPort);
    }

    @Test
    @DisplayName("Rechaza la cancelación si el usuario no es cliente")
    void rejectsNonClient() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(20L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNoEsClienteException.class, () -> service.ejecutar(COMANDO));
        verifyNoInteractions(reservaRepositoryPort);
    }

    @Test
    @DisplayName("Falla si la reserva no existe")
    void unknownReservation() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(20L)).thenReturn(Optional.of(3L));
        when(reservaRepositoryPort.buscarPorId(8)).thenReturn(Optional.empty());

        assertThrows(ReservaNoEncontradaException.class, () -> service.ejecutar(COMANDO));
    }

    @Test
    @DisplayName("Rechaza cancelar la reserva de otro cliente")
    void rejectsOtherClientsReservation() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(20L)).thenReturn(Optional.of(3L));
        when(reservaRepositoryPort.buscarPorId(8)).thenReturn(Optional.of(reservationOf(4L, AHORA.plusDays(1))));

        assertThrows(ReservaAjenaException.class, () -> service.ejecutar(COMANDO));
        verify(reservaRepositoryPort, never()).guardar(any());
    }
}
