package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import com.service_reservation.EAP03.modulos.recursos.domain.model.BloqueoRecurso;
import com.service_reservation.EAP03.modulos.recursos.domain.model.RecursoPrestado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BloqueoEnConflictoExceptionTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 1, 10, 0);
    private static final LocalDateTime FIN = LocalDateTime.of(2026, 10, 1, 12, 0);

    @Test
    @DisplayName("Describe el préstamo con el que choca el bloqueo")
    void conflictWithLoan() {
        // Arrange
        RecursoPrestado prestamo = RecursoPrestado.reconstruir(4, 1, 100, 2, INICIO, FIN, BigDecimal.TEN, null);

        // Act
        BloqueoEnConflictoException ex = BloqueoEnConflictoException.conPrestamo(prestamo);

        // Assert
        assertEquals("El bloqueo choca con un préstamo existente del recurso 1 (préstamo 4, de "
                + INICIO + " a " + FIN + ").", ex.getMessage());
    }

    @Test
    @DisplayName("Describe el bloqueo con el que choca el nuevo bloqueo")
    void conflictWithBlock() {
        // Arrange
        BloqueoRecurso bloqueo = BloqueoRecurso.reconstruir(6, 1, INICIO, FIN, "Mantenimiento", INICIO);

        // Act
        BloqueoEnConflictoException ex = BloqueoEnConflictoException.conBloqueo(bloqueo);

        // Assert
        assertEquals("El bloqueo choca con otro bloqueo del recurso 1 (bloqueo 6, de "
                + INICIO + " a " + FIN + ").", ex.getMessage());
    }
}
