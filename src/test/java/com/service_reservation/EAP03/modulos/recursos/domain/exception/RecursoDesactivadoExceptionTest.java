package com.service_reservation.EAP03.modulos.recursos.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecursoDesactivadoExceptionTest {

    @Test
    @DisplayName("Con id indica que no se puede asociar a un servicio")
    void cannotLinkMessage() {
        assertEquals("El recurso 3 está desactivado y no se puede asociar a un servicio.",
                new RecursoDesactivadoException(3).getMessage());
    }

    @Test
    @DisplayName("Al prestar indica el nombre del recurso desactivado")
    void cannotLendMessage() {
        assertEquals("No se puede prestar: el recurso 'Camilla' ya no está disponible porque el proveedor lo desactivó.",
                RecursoDesactivadoException.alPrestar("Camilla").getMessage());
    }

    @Test
    @DisplayName("Al reservar indica el nombre del recurso desactivado")
    void cannotReserveMessage() {
        assertEquals("No se puede reservar: el recurso 'Camilla' ya no está disponible porque el proveedor lo desactivó.",
                RecursoDesactivadoException.alReservar("Camilla").getMessage());
    }
}
