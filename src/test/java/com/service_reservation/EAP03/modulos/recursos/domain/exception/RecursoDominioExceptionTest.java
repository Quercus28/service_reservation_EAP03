package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RecursoDominioExceptionTest {

    @Test
    @DisplayName("Conserva el mensaje y es una excepción no comprobada")
    void keepsMessage() {
        RecursoDominioException ex = new RecursoDominioException("Error de dominio") {
        };

        assertEquals("Error de dominio", ex.getMessage());
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("Escribe 'unidad' en singular solo para una unidad")
    void formatsUnits() {
        assertEquals("1 unidad", RecursoDominioException.unidades(1));
        assertEquals("0 unidades", RecursoDominioException.unidades(0));
        assertEquals("2 unidades", RecursoDominioException.unidades(2));
    }
}
