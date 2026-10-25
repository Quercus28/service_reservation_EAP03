package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DisponibilidadInsuficienteExceptionTest {

    @Test
    @DisplayName("Usa el singular cuando se pidió y queda una sola unidad")
    void singularMessage() {
        // Act
        DisponibilidadInsuficienteException ex = new DisponibilidadInsuficienteException(1, 1, 1);

        // Assert
        assertEquals("No hay disponibilidad suficiente para el recurso 1: se solicitó 1 unidad"
                + " y solo hay 1 disponible en ese rango de tiempo.", ex.getMessage());
        assertEquals(1, ex.getDisponible());
    }

    @Test
    @DisplayName("Usa el plural cuando se piden y quedan varias unidades o ninguna")
    void pluralMessage() {
        // Act
        DisponibilidadInsuficienteException ex = new DisponibilidadInsuficienteException(1, 3, 0);

        // Assert
        assertEquals("No hay disponibilidad suficiente para el recurso 1: se solicitaron 3 unidades"
                + " y solo hay 0 disponibles en ese rango de tiempo.", ex.getMessage());
        assertEquals(0, ex.getDisponible());
    }
}
