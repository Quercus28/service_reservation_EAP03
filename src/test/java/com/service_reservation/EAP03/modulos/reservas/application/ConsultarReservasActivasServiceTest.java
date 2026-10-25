package com.service_reservation.EAP03.modulos.reservas.application;

import com.service_reservation.EAP03.modulos.reservas.domain.model.EstadoReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.IntervaloReservado;
import com.service_reservation.EAP03.modulos.reservas.domain.model.PartesReserva;
import com.service_reservation.EAP03.modulos.reservas.domain.model.Reserva;
import com.service_reservation.EAP03.modulos.reservas.domain.ports.out.ReservaRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarReservasActivasServiceTest {

    private static final LocalDateTime DESDE = LocalDateTime.of(2026, 11, 2, 8, 0);
    private static final LocalDateTime HASTA = LocalDateTime.of(2026, 11, 2, 12, 0);

    @Mock private ReservaRepositoryPort reservaRepositoryPort;

    @InjectMocks private ConsultarReservasActivasService service;

    private static Reserva reservation(int id, LocalDateTime inicio) {
        return Reserva.reconstruir(id, new PartesReserva(3L, 10, 7), new IntervaloReservado(inicio, inicio.plusHours(1)),
                EstadoReserva.RESERVADO, null, null, null);
    }

    @Test
    @DisplayName("Devuelve los intervalos de las reservas que realmente se cruzan con el rango")
    void returnsOverlappingIntervals() {
        // Arrange
        when(reservaRepositoryPort.buscarActivasQueSeSolapan(10, DESDE, HASTA)).thenReturn(List.of(
                reservation(1, DESDE.plusHours(1)), reservation(2, HASTA)));

        // Act
        List<IntervaloReservado> intervalos = service.ejecutar(10, DESDE, HASTA);

        // Assert
        assertEquals(List.of(new IntervaloReservado(DESDE.plusHours(1), DESDE.plusHours(2))), intervalos);
    }
}
