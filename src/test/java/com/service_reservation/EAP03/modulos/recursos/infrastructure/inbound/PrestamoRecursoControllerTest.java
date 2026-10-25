package com.service_reservation.EAP03.modulos.recursos.infrastructure.inbound;

import com.service_reservation.EAP03.modulos.recursos.domain.model.DisponibilidadRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.PeriodoPrestamo;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarPrestamoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.ConsultarDisponibilidadUseCase;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.in.RegistrarPrestamoRecursoUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrestamoRecursoControllerTest {

    private static final BigDecimal PRECIO = new BigDecimal("50000.00");
    private static final LocalDateTime DESDE = LocalDateTime.of(2026, 11, 1, 10, 0);
    private static final LocalDateTime HASTA = DESDE.plusHours(2);

    @Mock private ConsultarDisponibilidadUseCase consultarDisponibilidadUseCase;
    @Mock private RegistrarPrestamoRecursoUseCase registrarPrestamoRecursoUseCase;

    @InjectMocks private PrestamoRecursoController controller;

    @Test
    @DisplayName("Consultar disponibilidad devuelve el cálculo del caso de uso")
    void queriesAvailability() {
        // Arrange
        when(consultarDisponibilidadUseCase.ejecutar(1, DESDE, HASTA))
                .thenReturn(new DisponibilidadRecurso(1, DESDE, HASTA, 5, 2, 3, true));

        // Act
        DisponibilidadResponseDTO response = controller.consultarDisponibilidad(1, DESDE, HASTA);

        // Assert
        assertEquals(new DisponibilidadResponseDTO(1, DESDE, HASTA, 5, 2, 3, true), response);
    }

    @Test
    @DisplayName("Registrar préstamo devuelve el préstamo guardado")
    void registersLoan() {
        // Arrange
        RegistrarPrestamoComando comando = new RegistrarPrestamoComando(1, 100, 2, DESDE, HASTA, PRECIO);
        when(registrarPrestamoRecursoUseCase.ejecutar(comando))
                .thenReturn(RecursoPrestado.reconstruir(4, 1, 100, 2, new PeriodoPrestamo(DESDE, HASTA), PRECIO, null));

        // Act
        RecursoPrestadoResponseDTO response = controller.registrarPrestamo(1,
                new RegistrarPrestamoRequestDTO(100, 2, DESDE, HASTA, PRECIO));

        // Assert
        assertEquals(new RecursoPrestadoResponseDTO(4, 1, 100, 2, DESDE, HASTA, PRECIO), response);
    }
}
