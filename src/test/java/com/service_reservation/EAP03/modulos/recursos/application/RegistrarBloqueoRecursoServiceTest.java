package com.service_reservation.EAP03.modulos.recursos.application;

import com.service_reservation.EAP03.modulos.recursos.domain.exception.BloqueoEnConflictoException;
import com.service_reservation.EAP03.modulos.recursos.domain.exception.RecursoNoEncontradoException;
import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.Recurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RegistrarBloqueoComando;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.BloqueoRecursoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoPrestadoRepositoryPort;
import com.service_reservation.EAP03.modulos.recursos.domain.ports.out.RecursoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarBloqueoRecursoServiceTest {

    private static final LocalDateTime INICIO = LocalDateTime.now().plusDays(3).withNano(0);
    private static final LocalDateTime FIN = INICIO.plusHours(2);
    private static final RegistrarBloqueoComando COMANDO = new RegistrarBloqueoComando(1, INICIO, FIN, "Mantenimiento");

    @Mock private RecursoRepositoryPort recursoRepositoryPort;
    @Mock private BloqueoRecursoRepositoryPort bloqueoRecursoRepositoryPort;
    @Mock private RecursoPrestadoRepositoryPort recursoPrestadoRepositoryPort;

    @InjectMocks private RegistrarBloqueoRecursoService service;

    private void existingResource() {
        when(recursoRepositoryPort.buscarPorIdConBloqueo(1)).thenReturn(Optional.of(Recurso.nuevo(7, "Camilla", BigDecimal.TEN, 3)));
    }

    private static BloqueoRecurso block(LocalDateTime inicio, LocalDateTime fin) {
        return BloqueoRecurso.reconstruir(9, 1, inicio, fin, null, inicio.minusDays(1));
    }

    private static RecursoPrestado loan(LocalDateTime inicio, LocalDateTime fin) {
        return RecursoPrestado.reconstruir(4, 1, 100, 1, inicio, fin, BigDecimal.TEN, null);
    }

    @Test
    @DisplayName("Guarda el bloqueo cuando no choca con bloqueos ni préstamos")
    void savesBlockWithoutConflicts() {
        // Arrange
        existingResource();
        when(bloqueoRecursoRepositoryPort.buscarQueSeSolapan(1, INICIO, FIN)).thenReturn(List.of(block(FIN, FIN.plusHours(1))));
        when(recursoPrestadoRepositoryPort.buscarQueSeSolapan(1, INICIO, FIN)).thenReturn(List.of(loan(INICIO.minusHours(1), INICIO)));
        when(bloqueoRecursoRepositoryPort.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        BloqueoRecurso bloqueo = service.ejecutar(COMANDO);

        // Assert
        assertEquals(INICIO, bloqueo.getFechaInicio());
        assertEquals(FIN, bloqueo.getFechaFin());
        assertEquals("Mantenimiento", bloqueo.getMotivo());
    }

    @Test
    @DisplayName("Rechaza el bloqueo si choca con otro bloqueo")
    void rejectsConflictWithBlock() {
        existingResource();
        when(bloqueoRecursoRepositoryPort.buscarQueSeSolapan(1, INICIO, FIN)).thenReturn(List.of(block(INICIO.plusHours(1), FIN.plusHours(1))));

        BloqueoEnConflictoException ex = assertThrows(BloqueoEnConflictoException.class, () -> service.ejecutar(COMANDO));

        assertTrue(ex.getMessage().startsWith("El bloqueo choca con otro bloqueo"));
        verifyNoInteractions(recursoPrestadoRepositoryPort);
        verify(bloqueoRecursoRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("Rechaza el bloqueo si choca con un préstamo")
    void rejectsConflictWithLoan() {
        existingResource();
        when(bloqueoRecursoRepositoryPort.buscarQueSeSolapan(1, INICIO, FIN)).thenReturn(List.of());
        when(recursoPrestadoRepositoryPort.buscarQueSeSolapan(1, INICIO, FIN)).thenReturn(List.of(loan(INICIO.minusHours(1), INICIO.plusHours(1))));

        BloqueoEnConflictoException ex = assertThrows(BloqueoEnConflictoException.class, () -> service.ejecutar(COMANDO));

        assertTrue(ex.getMessage().startsWith("El bloqueo choca con un préstamo"));
        verify(bloqueoRecursoRepositoryPort, never()).guardar(any());
    }

    @Test
    @DisplayName("Falla si el recurso no existe")
    void unknownResource() {
        when(recursoRepositoryPort.buscarPorIdConBloqueo(1)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.ejecutar(COMANDO));
        verifyNoInteractions(bloqueoRecursoRepositoryPort, recursoPrestadoRepositoryPort);
    }
}
