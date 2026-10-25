package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecursoNoOperativoExceptionTest {

    @Test
    @DisplayName("Indica el recurso y el rango en el que no está operativo")
    void describesRange() {
        // Arrange
        LocalDateTime desde = LocalDateTime.of(2026, 10, 1, 10, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 10, 1, 12, 0);

        // Act
        RecursoNoOperativoException ex = new RecursoNoOperativoException(1, desde, hasta);

        // Assert
        assertEquals("El recurso 1 no está operativo entre " + desde + " y " + hasta
                + ": el rango queda fuera de su horario o dentro de un bloqueo.", ex.getMessage());
    }
}
