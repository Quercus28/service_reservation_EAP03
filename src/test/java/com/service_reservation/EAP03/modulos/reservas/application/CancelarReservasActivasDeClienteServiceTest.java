package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarClienteUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.LiberarRecursosDeReservaUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.IntervaloReservado;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PartesReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.out.ReservaRepositoryPort;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelarReservasActivasDeClienteServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 24, 12, 0);

    @Mock private ConsultarClienteUseCase consultarClienteUseCase;
    @Mock private ReservaRepositoryPort reservaRepositoryPort;
    @Mock private LiberarRecursosDeReservaUseCase liberarRecursosDeReservaUseCase;

    private CancelarReservasActivasDeClienteService service;

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(Instant.parse("2026-10-24T12:00:00Z"), ZoneOffset.UTC);
        service = new CancelarReservasActivasDeClienteService(consultarClienteUseCase, reservaRepositoryPort,
                liberarRecursosDeReservaUseCase, reloj);
    }

    private static Reserva active(int id, EstadoReserva estado) {
        return Reserva.reconstruir(id, new PartesReserva(3L, 10, 7), new IntervaloReservado(AHORA.plusMinutes(30), AHORA.plusMinutes(90)),
                estado, null, AHORA, null);
    }

    @Test
    @DisplayName("Cancela todas las reservas activas del cliente con la misma hora y libera sus recursos")
    void cancelsAllActiveReservations() {
        // Arrange
        Reserva reservada = active(1, EstadoReserva.RESERVADO);
        Reserva enProceso = active(2, EstadoReserva.EN_PROCESO);
        when(consultarClienteUseCase.buscarIdClientePorUsuario(20L)).thenReturn(Optional.of(3L));
        when(reservaRepositoryPort.buscarActivasDeCliente(3L)).thenReturn(List.of(reservada, enProceso));

        // Act
        int canceladas = service.ejecutar(20L, "REVOCACION_ADMIN");

        // Assert
        assertEquals(2, canceladas);
        assertEquals(AHORA, reservada.getCanceladaEn());
        assertEquals(AHORA, enProceso.getCanceladaEn());
        assertEquals("REVOCACION_ADMIN", enProceso.getMotivoCancelacion());
        verify(reservaRepositoryPort).guardar(reservada);
        verify(reservaRepositoryPort).guardar(enProceso);
        verify(liberarRecursosDeReservaUseCase).ejecutar(1);
        verify(liberarRecursosDeReservaUseCase).ejecutar(2);
    }

    @Test
    @DisplayName("Devuelve cero si el usuario no es cliente o no se indica usuario")
    void returnsZeroWithoutClient() {
        when(consultarClienteUseCase.buscarIdClientePorUsuario(20L)).thenReturn(Optional.empty());

        assertEquals(0, service.ejecutar(20L, "REVOCACION_ADMIN"));
        assertEquals(0, service.ejecutar(null, "REVOCACION_ADMIN"));
        verifyNoInteractions(reservaRepositoryPort, liberarRecursosDeReservaUseCase);
    }
}
