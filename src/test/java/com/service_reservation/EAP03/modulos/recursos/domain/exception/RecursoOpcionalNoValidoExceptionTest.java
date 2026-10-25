package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecursoOpcionalNoValidoExceptionTest {

    @Test
    @DisplayName("Con un solo recurso usa el singular")
    void singleResource() {
        assertEquals("No se puede pedir el recurso 'Toalla' con el servicio 'Masaje': no está entre sus recursos opcionales.",
                new RecursoOpcionalNoValidoException("'Masaje'", List.of("'Toalla'")).getMessage());
    }

    @Test
    @DisplayName("Con varios recursos los enumera en plural")
    void severalResources() {
        assertEquals("No se pueden pedir los recursos 'Toalla', 7 con el servicio 10: no están entre sus recursos opcionales.",
                new RecursoOpcionalNoValidoException("10", List.of("'Toalla'", "7")).getMessage());
    }
}
