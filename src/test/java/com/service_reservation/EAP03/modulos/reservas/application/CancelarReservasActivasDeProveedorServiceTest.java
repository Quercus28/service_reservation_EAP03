package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.identidad.domain.ports.in.ConsultarProveedorUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.LiberarRecursosDeReservaUseCase;
import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
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
class CancelarReservasActivasDeProveedorServiceTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 24, 12, 0);

    @Mock private ConsultarProveedorUseCase consultarProveedorUseCase;
    @Mock private ReservaRepositoryPort reservaRepositoryPort;
    @Mock private LiberarRecursosDeReservaUseCase liberarRecursosDeReservaUseCase;

    private CancelarReservasActivasDeProveedorService service;

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(Instant.parse("2026-10-24T12:00:00Z"), ZoneOffset.UTC);
        service = new CancelarReservasActivasDeProveedorService(consultarProveedorUseCase, reservaRepositoryPort,
                liberarRecursosDeReservaUseCase, reloj);
    }

    @Test
    @DisplayName("Cancela todas las reservas activas del proveedor y libera sus recursos")
    void cancelsAllActiveReservations() {
        // Arrange
        Reserva reserva = Reserva.reconstruir(1, 3L, 10, 7, AHORA.plusDays(1), AHORA.plusDays(1).plusHours(1),
                EstadoReserva.RESERVADO, null, AHORA, null, null);
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(30L)).thenReturn(Optional.of(7));
        when(reservaRepositoryPort.buscarActivasDeProveedor(7)).thenReturn(List.of(reserva));

        // Act
        int canceladas = service.ejecutar(30L, "REVOCACION_ADMIN");

        // Assert
        assertEquals(1, canceladas);
        assertEquals(EstadoReserva.CANCELADO, reserva.getEstado());
        assertEquals(AHORA, reserva.getCanceladaEn());
        verify(reservaRepositoryPort).guardar(reserva);
        verify(liberarRecursosDeReservaUseCase).ejecutar(1);
    }

    @Test
    @DisplayName("Devuelve cero si el usuario no es proveedor o no se indica usuario")
    void returnsZeroWithoutProvider() {
        when(consultarProveedorUseCase.buscarIdProveedorPorUsuario(30L)).thenReturn(Optional.empty());

        assertEquals(0, service.ejecutar(30L, "REVOCACION_ADMIN"));
        assertEquals(0, service.ejecutar(null, "REVOCACION_ADMIN"));
        verifyNoInteractions(reservaRepositoryPort, liberarRecursosDeReservaUseCase);
    }
}
