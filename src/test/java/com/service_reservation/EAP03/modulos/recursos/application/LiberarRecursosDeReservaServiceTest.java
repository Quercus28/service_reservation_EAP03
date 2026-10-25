package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoPrestadoRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiberarRecursosDeReservaServiceTest {

    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-10-24T15:00:00Z"), ZoneOffset.UTC);
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 24, 15, 0);

    @Mock private RecursoPrestadoRepositoryPort recursoPrestadoRepositoryPort;

    private LiberarRecursosDeReservaService service;

    @BeforeEach
    void setUp() {
        service = new LiberarRecursosDeReservaService(recursoPrestadoRepositoryPort, RELOJ);
    }

    private static RecursoPrestado loan(int idRecurso) {
        LocalDateTime inicio = LocalDateTime.of(2026, 11, 1, 10, 0);
        return RecursoPrestado.nuevo(idRecurso, 50, 1, inicio, inicio.plusHours(1), BigDecimal.TEN);
    }

    @Test
    @DisplayName("Libera con la hora actual todos los préstamos activos de la reserva")
    void releasesAllActiveLoans() {
        // Arrange
        RecursoPrestado primero = loan(1);
        RecursoPrestado segundo = loan(2);
        when(recursoPrestadoRepositoryPort.buscarActivosDeReserva(50)).thenReturn(List.of(primero, segundo));

        // Act
        service.ejecutar(50);

        // Assert
        assertEquals(AHORA, primero.getLiberadoEn());
        assertEquals(AHORA, segundo.getLiberadoEn());
        verify(recursoPrestadoRepositoryPort).guardar(primero);
        verify(recursoPrestadoRepositoryPort).guardar(segundo);
    }

    @Test
    @DisplayName("Sin reserva no hace nada")
    void nullReservationDoesNothing() {
        service.ejecutar(null);

        verifyNoInteractions(recursoPrestadoRepositoryPort);
    }
}
