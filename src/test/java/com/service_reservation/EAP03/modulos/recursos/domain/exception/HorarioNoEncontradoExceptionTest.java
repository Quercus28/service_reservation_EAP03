package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class HorarioNoEncontradoExceptionTest {

    @Test
    @DisplayName("Indica el horario y el recurso que no se encontraron")
    void buildsMessage() {
        HorarioNoEncontradoException ex = new HorarioNoEncontradoException(1, 5);

        assertEquals("No se encontró el horario con ID 5 para el recurso 1", ex.getMessage());
        assertInstanceOf(RecursoDominioException.class, ex);
    }
}
