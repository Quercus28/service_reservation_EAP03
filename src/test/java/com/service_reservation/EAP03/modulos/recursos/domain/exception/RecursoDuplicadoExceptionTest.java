package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RecursoDuplicadoExceptionTest {

    @Test
    @DisplayName("Indica el nombre duplicado")
    void buildsMessage() {
        RecursoDuplicadoException ex = new RecursoDuplicadoException("Camilla");

        assertEquals("Ya existe un recurso activo con el nombre 'Camilla' en este proveedor.", ex.getMessage());
        assertInstanceOf(RecursoDominioException.class, ex);
    }
}
