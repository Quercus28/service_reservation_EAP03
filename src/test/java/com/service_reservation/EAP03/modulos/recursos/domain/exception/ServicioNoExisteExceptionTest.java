package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ServicioNoExisteExceptionTest {

    @Test
    @DisplayName("Indica el id del servicio que no se encontró")
    void buildsMessage() {
        ServicioNoExisteException ex = new ServicioNoExisteException(10);

        assertEquals("No se encontró el servicio con ID: 10", ex.getMessage());
        assertInstanceOf(RecursoDominioException.class, ex);
    }
}
